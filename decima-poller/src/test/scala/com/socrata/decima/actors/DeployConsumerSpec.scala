package com.socrata.decima.actors

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestActorRef, TestKit}
import com.socrata.decima.actors.DeployConsumer.DeployMessage
import com.socrata.decima.actors.SqsActor.SqsMessageMetadata
import com.socrata.decima.actors.SqsMessageFinalizer.FinalizeSqsMessage
import com.socrata.decima.data_access.DeploymentAccess
import com.socrata.decima.data_access.DeploymentAccess.{DeployCreated, DuplicateDeploy}
import com.socrata.decima.models.Deploy
import org.joda.time.DateTime
import org.scalamock.scalatest.MockFactory
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.util.{Failure, Success}


class DeployConsumerSpec extends TestKit(ActorSystem("testSystem")) with WordSpecLike with Matchers with BeforeAndAfter
  with BeforeAndAfterAll with ImplicitSender with MockFactory {

  val messageMetadata = SqsMessageMetadata(null)
  class UnexpectedException(message: String) extends Exception(message)

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
    deployedAt = DateTime.now
  )

  def withMocks(f: (DeploymentAccess, TestActorRef[_ <: akka.actor.Actor]) => Unit) = {
    val deploymentAccess = mock[DeploymentAccess]
    val consumerRef = TestActorRef(new DeployConsumer(deploymentAccess, self))
    f(deploymentAccess, consumerRef)
    consumerRef.stop()
  }

  "A DeployConsumer" should {
    "process a valid deploy" in {
      withMocks { case (deploymentAccess, consumerRef) =>
        (deploymentAccess.createDeploy _)
          .expects(testDeploy)
          .returning(Success(DeployCreated(testDeploy.copy(id = 123))))

        consumerRef ! DeployMessage(testDeploy, messageMetadata)
        expectMsg(FinalizeSqsMessage(messageMetadata))
      }
    }

    "handle a duplicate deploy" in {
      withMocks { case (deploymentAccess, consumerRef) =>
        (deploymentAccess.createDeploy _)
          .expects(testDeploy)
          .returning(Success(DuplicateDeploy(testDeploy.service, testDeploy.environment, testDeploy.deployedAt)))

        consumerRef ! DeployMessage(testDeploy, messageMetadata)
        expectMsg(FinalizeSqsMessage(messageMetadata))
      }
    }

    "die without sending messages during an unexpected failure" in {
      withMocks { case (deploymentAccess, consumerRef) =>
        (deploymentAccess.createDeploy _)
          .expects(testDeploy)
          .returning(Failure(new UnexpectedException("Oh noes postgres is dead")))

        intercept[UnexpectedException] { consumerRef.receive(DeployMessage(testDeploy, messageMetadata)) }
        expectNoMsg()
      }
    }
  }
}
