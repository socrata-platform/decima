package com.socrata.decima.actors

import java.util.concurrent.TimeUnit

import akka.actor.{Actor, ActorLogging}
import com.amazonaws.services.sqs.{AmazonSQSAsync, AmazonSQSAsyncClient}
import com.socrata.decima.config.SqsConfig

object SqsActor {
  case class SqsMessageMetadata(receiptHandle: String)
}

abstract class SqsActor(sqsClient: AmazonSQSAsync, config: SqsConfig) extends Actor with ActorLogging {
  val queueUrl: String = sqsClient.getQueueUrl(config.queueName).getQueueUrl
  log.info(s"Initialized SQS client. Queue URL: $queueUrl")

  override def postStop(): Unit = {
    log.info("Shutting down SQS client")
    if(sqsClient.getClass == classOf[AmazonSQSAsyncClient]){
      val client = sqsClient.asInstanceOf[AmazonSQSAsyncClient]
      val executorService = client.getExecutorService
      executorService.shutdown()
      log.info("Initiated shutdown for SQS executor service")
      executorService.awaitTermination(config.pollTimeout + 1, TimeUnit.SECONDS)
      log.info("Shutdown complete for SQS client")
    } else {
      log.info("Unable to get the executor service, shutting down anyways.")
    }
  }

  protected def die(): Unit = context.system.stop(self)
}
