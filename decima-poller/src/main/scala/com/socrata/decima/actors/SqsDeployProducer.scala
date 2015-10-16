package com.socrata.decima.actors

import java.util.concurrent.Future

import akka.actor.{ActorLogging, Actor}
import com.amazonaws.handlers.AsyncHandler
import com.amazonaws.services.sqs.{AmazonSQSAsyncClient, AmazonSQSAsync}
import com.amazonaws.services.sqs.model.{DeleteMessageRequest, ReceiveMessageRequest, ReceiveMessageResult}
import com.fasterxml.jackson.core.JsonParseException
import com.socrata.decima.config.PollerAwsConfig
import com.socrata.decima.util.JsonFormats
import org.json4s.MappingException
import org.json4s.jackson.Serialization._
import com.socrata.decima.actors.DeployConsumer.{DeployMessageMetadata, DeployProcessedMessage, DeployMessage}
import com.socrata.decima.models.Deploy

import scala.collection.JavaConverters._

object SqsDeployProducer {
  case object PollDeploysMessage
  case class SqsDeployMetadata(receiptHandle: String) extends DeployMessageMetadata
  protected case class SqsRequestFailed(ex: Exception)
}

class SqsDeployProducer(config: PollerAwsConfig) extends Actor
  with ActorLogging {
  import SqsDeployProducer._

  implicit val jsonFormats = JsonFormats.Formats

  private var messageReceiveFuture: Future[ReceiveMessageResult] = _
  private var messageDeleteFuture: Future[Void] = _
  private var sqsClient: AmazonSQSAsync = _
  private var queueUrl: String = _

  override def preStart() = {
    log.debug("Initializing SQS client")
    sqsClient = new AmazonSQSAsyncClient(config.credentials, config.clientConfig)
                     .withEndpoint(config.sqs.baseEndpoint)
    log.info("Resolving SQS queue url")
    queueUrl = sqsClient.getQueueUrl(config.sqs.queueName).getQueueUrl
    log.info(s"Initialized SQS client. Queue URL: $queueUrl")
  }

  override def postStop() = {
    log.info("Shutdown received. Cancelling any in-flight SQS requests")
    if (messageReceiveFuture != null && messageReceiveFuture.cancel(true)) {
      log.warning("Cancelled in-flight receive request")
    }
    if (messageDeleteFuture != null && messageDeleteFuture.cancel(true)) {
      log.warning("Cancelled in-flight delete request")
    }
    log.info("Finished cleaning up in-flight requests, shutting down client")
    sqsClient.shutdown()
    log.info("Completed shutdown of SQS client")
  }

  def receive = {
    case PollDeploysMessage =>
      // Kickoff an async request to SQS
      log.debug(s"Polling for events on queue: $queueUrl")
      val request = new ReceiveMessageRequest(queueUrl)
                          .withMaxNumberOfMessages(config.sqs.messagesPerPoll)
                          .withWaitTimeSeconds(config.sqs.pollTimeout)
                          .withVisibilityTimeout(config.sqs.visibilityTimeout)
      messageReceiveFuture = sqsClient.receiveMessageAsync(request, MessageReceivedHandler)
    case DeployProcessedMessage(metadata: SqsDeployMetadata) =>
      // Message is finished being processed; safe to delete
      log.debug(s"Deploy message processed. Handle: '${metadata.receiptHandle}'. Deleting associated message.")
      val request = new DeleteMessageRequest(queueUrl, metadata.receiptHandle)
      messageDeleteFuture = sqsClient.deleteMessageAsync(request, MessageDeletedHandler)
  }

  private def die() = context.system.stop(self)

  private object MessageReceivedHandler extends AsyncHandler[ReceiveMessageRequest, ReceiveMessageResult] {
    def onSuccess(request: ReceiveMessageRequest, result: ReceiveMessageResult) = {
      result.getMessages.asScala.foreach { message =>
        val body = message.getBody
        val metadata = SqsDeployMetadata(message.getReceiptHandle)
        log.info(s"Received queue message. Id: '${message.getMessageId}'")
        log.debug(s"Message Id: '${message.getMessageId}'. Message body: '${message.getBody}'")
        try {
          val deploy = read[Deploy](body)
          log.debug(s"Successfully parsed deploy: $deploy")
          context.actorSelection("../consumer") ! DeployMessage(deploy, metadata)
        } catch {
          case _: MappingException | _: JsonParseException =>
            log.error(s"Unable to parse message body: '$body'. Signalling complete processing")
            self ! DeployProcessedMessage(metadata)
        }
      }
      // Resume polling for new events
      self ! PollDeploysMessage
    }

    def onError(ex: Exception) = {
      log.error(ex, "Exception while receiving message from SQS")
      die()
    }
  }

  private object MessageDeletedHandler extends AsyncHandler[DeleteMessageRequest, Void] {
    override def onSuccess(request: DeleteMessageRequest, result: Void): Unit = {
      log.debug(s"Successfully deleted message with receipt '${request.getReceiptHandle}'")
    }

    override def onError(ex: Exception): Unit = {
      log.error(ex, "Exception while deleting message from SQS")
      die()
    }
  }
}
