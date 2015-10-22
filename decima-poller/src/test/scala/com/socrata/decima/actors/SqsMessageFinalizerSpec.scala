package com.socrata.decima.actors

import java.util.concurrent._

import akka.actor.ActorSystem
import akka.testkit.{EventFilter, ImplicitSender, TestActorRef, TestKit}
import com.amazonaws.handlers._
import com.amazonaws.services.sqs.AmazonSQSAsync
import com.amazonaws.services.sqs.model.{DeleteMessageRequest, GetQueueUrlResult}
import com.socrata.decima.actors.SqsActor.SqsMessageMetadata
import com.socrata.decima.actors.SqsMessageFinalizer.FinalizeSqsMessage
import com.socrata.decima.config.PollerAwsConfig
import com.typesafe.config.ConfigFactory
import org.scalamock.scalatest.MockFactory
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, Matchers, WordSpecLike}

class SqsMessageFinalizerSpec extends TestKit(ActorSystem("testSystem")) with WordSpecLike with Matchers with BeforeAndAfter
with BeforeAndAfterAll with ImplicitSender with MockFactory {
  val queueUrl = "http://queue"

  def withMocks(f: (AmazonSQSAsync, TestActorRef[_ <: akka.actor.Actor]) => Unit) = {
    val client = mock[AmazonSQSAsync]
    (client.getQueueUrl(_: String)).expects(*).returning(new GetQueueUrlResult().withQueueUrl(queueUrl))
    val config = new PollerAwsConfig(ConfigFactory.load.getConfig("aws"))
    val messageFinalizerRef = TestActorRef(new SqsMessageFinalizer(client, config.sqs))
    f(client, messageFinalizerRef)
    messageFinalizerRef.stop()
  }

  val executorService = Executors.newFixedThreadPool(5)

  class UnexpectedException(message: String) extends Exception(message)

  "An SqsMessageFinalizer" should {
    "finalize a message after it has been processed" in {
      withMocks { case (client, messageFinalizerRef) =>
        (client.deleteMessageAsync(_: DeleteMessageRequest, _: AsyncHandler[DeleteMessageRequest, Void]))
          .expects(new DeleteMessageRequest(queueUrl, "message receipt"), *)
          .onCall { (request: DeleteMessageRequest, handler: AsyncHandler[DeleteMessageRequest, Void]) =>
            executorService.submit(new Callable[Void] {
              override def call(): Void = {
                Thread.sleep(500)
                handler.onSuccess(request, null)
                null
              }
            })
        }

        val finalizeMessage = FinalizeSqsMessage(SqsMessageMetadata("message receipt"))
        messageFinalizerRef ! finalizeMessage
        expectNoMsg()
      }
    }

    "log an error message if it fails to delete a message" in {
      withMocks { case (client, messageFinalizerRef) =>
        (client.deleteMessageAsync(_: DeleteMessageRequest, _: AsyncHandler[DeleteMessageRequest, Void]))
          .expects(new DeleteMessageRequest(queueUrl, "invalid receipt"), *)
          .onCall { (request: DeleteMessageRequest, handler: AsyncHandler[DeleteMessageRequest, Void]) =>
            executorService.submit(new Callable[Void] {
              override def call(): Void = {
                Thread.sleep(500)
                val exception = new UnexpectedException("An unexpected error occurred!")
                handler.onError(exception)
                throw exception
              }
            })
        }

        EventFilter[UnexpectedException](occurrences = 1) intercept {
          val finalizeMessage = FinalizeSqsMessage(SqsMessageMetadata("invalid receipt"))
          messageFinalizerRef ! finalizeMessage
        }
      }
    }
  }
}
