package com.socrata.decima.actors

import java.util.concurrent.{ExecutionException, Future}

import akka.actor.ActorRef
import com.amazonaws.services.sqs.AmazonSQSAsync
import com.amazonaws.services.sqs.model.{ReceiveMessageRequest, ReceiveMessageResult}
import com.fasterxml.jackson.core.JsonParseException
import com.netflix.servo.monitor._
import com.socrata.decima.actors.DeployConsumer.DeployMessage
import com.socrata.decima.actors.SqsActor.SqsMessageMetadata
import com.socrata.decima.actors.SqsMessageFinalizer.FinalizeSqsMessage
import com.socrata.decima.config.SqsConfig
import com.socrata.decima.models.Deploy
import com.socrata.decima.util.JsonFormats
import org.json4s._
import org.json4s.jackson.JsonMethods._

import scala.collection.JavaConverters._

object SqsMessageReader {
  case class PollDeploysMessage(poller: ActorRef)
}

class SqsMessageReader(sqsClient: AmazonSQSAsync,
                       config: SqsConfig,
                       deployConsumer: ActorRef,
                       messageFinalizer: ActorRef)
  extends SqsActor(sqsClient, config) {
  import SqsMessageReader._ // scalastyle:ignore import.grouping

  val messagesRecieved: Counter = Monitors.newCounter("sqsMessagesRecieved")
  val invalidMessages: Counter = Monitors.newCounter("sqsMessagesInvalid")
  val missingKey: Counter = Monitors.newCounter("sqsMessagesMissingKey")
  val messageSuccess: Counter = Monitors.newCounter("sqsMessagesSuccess")
  Monitors.registerObject(this)

  implicit val jsonFormats = JsonFormats.Formats
  var messageReceivedFuture: Future[ReceiveMessageResult] = _

  override def postStop(): Unit = {
    if (messageReceivedFuture.cancel(true)) {
      log.warning("Cancelled in-flight receive request")
    }
    super.postStop()
  }

  def receive: PartialFunction[Any, Unit] = {
    case PollDeploysMessage(poller) =>
      // Kickoff an async request to SQS
      log.debug("Polling for events on queue")
      val request = new ReceiveMessageRequest(queueUrl)
        .withMaxNumberOfMessages(config.messagesPerPoll)
        .withWaitTimeSeconds(config.pollTimeout)
        .withVisibilityTimeout(config.visibilityTimeout)
      messageReceivedFuture = sqsClient.receiveMessageAsync(request)
      try {
        val result = messageReceivedFuture.get()
        val messages = result.getMessages.asScala
        log.debug(s"Received ${messages.length} new deploy messages.")
        messagesRecieved.increment(messages.length)
        messages.foreach { message =>
          val body = message.getBody
          val metadata = SqsMessageMetadata(message.getReceiptHandle)
          log.info(s"Received queue message. Id: '${message.getMessageId}'")
          log.debug(s"Message Id: '${message.getMessageId}'. Message body: '${message.getBody}'")
          try {
            val json = parse(body).camelizeKeys
            val deployedAt = json \ "deployedAt"
            log.info(s"deployedAt field is $deployedAt")
            if (deployedAt == JNothing) {
              missingKey.increment(1)
              log.error("Deploy does not contain deployed_at key, refusing to process message!")
              messageFinalizer ! FinalizeSqsMessage(metadata)
            } else {
              messageSuccess.increment(1)
              val deploy = json.extract[Deploy]
              log.debug(s"Successfully parsed deploy: $deploy")
              deployConsumer ! DeployMessage(deploy, metadata)
            }
          } catch {
            case ex@(_: MappingException | _: JsonParseException) =>
              invalidMessages.increment(1)
              log.error(s"Unable to parse message body: '$body'. Signalling complete processing")
              log.error(s"Exception: $ex")
              messageFinalizer ! FinalizeSqsMessage(metadata)
          }
        }
        poller ! PollDeploysMessage(poller)
      } catch {
        case ex: InterruptedException =>
          log.warning(s"Received InterruptedException, system is shutting down: $ex")
        case ex: ExecutionException =>
          log.error(ex.getCause, "Unexpected exception thrown while receiving messages from SQS!")
          die()
      }
  }
}
