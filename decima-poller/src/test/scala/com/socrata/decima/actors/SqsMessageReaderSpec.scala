package com.socrata.decima.actors

import java.util.concurrent._

import akka.actor.{ActorSystem, Terminated}
import akka.testkit.{EventFilter, ImplicitSender, TestActorRef, TestKit}
import com.amazonaws.services.sqs.AmazonSQSAsync
import com.amazonaws.services.sqs.model._
import com.socrata.decima.actors.DeployConsumer.DeployMessage
import com.socrata.decima.actors.SqsActor.SqsMessageMetadata
import com.socrata.decima.actors.SqsMessageFinalizer.FinalizeSqsMessage
import com.socrata.decima.actors.SqsMessageReader.PollDeploysMessage
import com.socrata.decima.config.{PollerAwsConfig, SqsConfig}
import com.socrata.decima.models.Deploy
import com.socrata.decima.util.JsonFormats
import com.typesafe.config.ConfigFactory
import org.joda.time.DateTime
import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.json4s.jackson.Serialization.write
import org.scalamock.scalatest.MockFactory
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.collection.JavaConverters._

class SqsMessageReaderSpec extends TestKit(ActorSystem("testSystem")) with WordSpecLike with Matchers with BeforeAndAfter
  with BeforeAndAfterAll with ImplicitSender with MockFactory {

  implicit val jsonFormats = JsonFormats.Formats

  val executorService = Executors.newFixedThreadPool(5)
  val queueUrl = "http://queue"

  val testDeploy = Deploy(
    "blahblah",
    "staging",
    "2.3.4",
    Option("1.2.3_123_f6b46bd0"),
    "f6b46bd0",
    None,
    Option("blah blah blah"),
    "apps-marathon:deploy",
    "jenkisdfns",
    deployedAt = DateTime.parse("2015-10-26T09:46:05-07:00")
  )

  val malformedDeployString = """{"service": "blah blah"}"""

  val config: SqsConfig = new SqsConfig(ConfigFactory.load.getConfig("aws.sqs"))
  val receiveRequest = new ReceiveMessageRequest(queueUrl)
                              .withMaxNumberOfMessages(config.messagesPerPoll)
                              .withWaitTimeSeconds(config.pollTimeout)
                              .withVisibilityTimeout(config.visibilityTimeout)

  def deployToJson(deploy: Deploy): String = {
    write(parse(write(deploy)).underscoreKeys)
  }

  def withMocks(f: (AmazonSQSAsync, TestActorRef[_ <: akka.actor.Actor]) => Unit) = {
    val client = mock[AmazonSQSAsync]
    (client.getQueueUrl(_: String)).expects(*).returning(new GetQueueUrlResult().withQueueUrl(queueUrl))
    val config = new PollerAwsConfig(ConfigFactory.load.getConfig("aws"))
    val messageReaderRef = TestActorRef(new SqsMessageReader(client, config.sqs, self, self))
    f(client, messageReaderRef)
    messageReaderRef.stop()
  }

  class UnexpectedException(message: String) extends Exception(message)

  "An SqsMessageReader" should {
    "parse deploys found in queue" in {
      withMocks{ (client, messageReaderRef) =>
        (client.receiveMessageAsync(_: ReceiveMessageRequest))
          .expects(receiveRequest)
          .onCall { request: ReceiveMessageRequest =>
            executorService.submit(new Callable[ReceiveMessageResult] {
              override def call(): ReceiveMessageResult = {
                Thread.sleep(500)
                val result = new ReceiveMessageResult()
                  .withMessages(List(new Message()
                  .withBody(deployToJson(testDeploy))
                  .withMessageId("test id")
                  .withReceiptHandle("test handle")).asJavaCollection)
                result
              }
            })
        }

        messageReaderRef ! PollDeploysMessage(self)
        expectMsgClass(classOf[DeployMessage])
        expectMsg(PollDeploysMessage(self))
      }
    }


    "finalize invalid deploys immediately" in {
      withMocks { (client, messageReaderRef) =>
        (client.receiveMessageAsync(_: ReceiveMessageRequest))
          .expects(receiveRequest)
          .onCall { request: ReceiveMessageRequest =>
            executorService.submit(new Callable[ReceiveMessageResult] {
              override def call(): ReceiveMessageResult = {
                Thread.sleep(500)
                val result = new ReceiveMessageResult()
                  .withMessages(List(new Message()
                  .withBody(malformedDeployString)
                  .withMessageId("invalid id")
                  .withReceiptHandle("invalid handle")).asJavaCollection)
                result
              }
            })
        }

        messageReaderRef ! PollDeploysMessage(self)
        expectMsg(FinalizeSqsMessage(SqsMessageMetadata("invalid handle")))
        expectMsg(PollDeploysMessage(self))
      }
    }

    "handle unexpected errors by logging exception and dying" in {
      withMocks { (client, messageReaderRef) =>
        (client.receiveMessageAsync(_: ReceiveMessageRequest))
          .expects(receiveRequest)
          .onCall { request: ReceiveMessageRequest =>
            executorService.submit(new Callable[ReceiveMessageResult] {
              override def call(): ReceiveMessageResult = {
                Thread.sleep(500)
                throw new UnexpectedException("An unexpected exception occurred!")
              }
            })
        }
        watch(messageReaderRef)

        EventFilter[UnexpectedException](occurrences = 1) intercept {
          messageReaderRef ! PollDeploysMessage(self)
        }

        expectMsgClass(classOf[Terminated])
      }
    }

    "require deploys to have timestamp set in queue to avoid async issues" in {
      withMocks { (client, messageReaderRef) =>
        val noTimeDeploy =
          """{
            |"service":"blahblah",
            |"environment":"staging",
            |"version":"2.3.4",
            |"docker_tag":"1.2.3_123_f6b46bd0",
            |"service_sha":"f6b46bd0",
            |"configuration":"blah blah blah",
            |"deployed_by":"apps-marathon:deploy",
            |"deploy_method":"jenkisdfns"
            |}""".stripMargin

        (client.receiveMessageAsync(_: ReceiveMessageRequest))
          .expects(receiveRequest)
          .onCall { request: ReceiveMessageRequest =>
            executorService.submit(new Callable[ReceiveMessageResult] {
              override def call(): ReceiveMessageResult = {
                Thread.sleep(500)
                val result = new ReceiveMessageResult()
                  .withMessages(List(new Message()
                  .withBody(noTimeDeploy)
                  .withMessageId("no time deploy id")
                  .withReceiptHandle("no time deploy handle")).asJavaCollection)
                result
              }
            })
        }

        messageReaderRef ! PollDeploysMessage(self)
        expectMsg(FinalizeSqsMessage(SqsMessageMetadata("no time deploy handle")))
        expectMsg(PollDeploysMessage(self))
      }
    }
  }
}
