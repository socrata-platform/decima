package com.socrata.decima.actors

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfter, Matchers, WordSpecLike}

class SqsDeployProducerSpec extends TestKit(ActorSystem("testSystem")) with WordSpecLike with Matchers with BeforeAndAfter
  with BeforeAndAfterAll with ImplicitSender with MockFactory {

  "An SqsDeployProducer" should {
    "Begin polling when it receives a PollDeploysMessage" in {
      fail("Not implemented")
    }

    "Send an SQS delete when it receives a DeployProcessedMessage" in {
      fail("Not implemented")
    }

    "Send a DeployMessage to a consumer when it receives polled data" in {
      fail("Not implemented")
    }

    "Send itself a PollDeploysMessage when it receives polled data" in {
      fail("Not implemented")
    }

    "Send itself a DeployProcessedMessage if it receives a malformed queue message" in {
      fail("Not implemented")
    }
  }
}
