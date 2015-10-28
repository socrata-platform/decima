package com.socrata.decima.actors


import com.amazonaws.handlers.AsyncHandler
import com.amazonaws.services.sqs.AmazonSQSAsync
import com.amazonaws.services.sqs.model.DeleteMessageRequest
import com.socrata.decima.actors.SqsActor.SqsMessageMetadata
import com.socrata.decima.config.SqsConfig

object SqsMessageFinalizer {
  case class FinalizeSqsMessage(metadata: SqsMessageMetadata)
}

class SqsMessageFinalizer(sqsClient: AmazonSQSAsync, config: SqsConfig)
  extends SqsActor(sqsClient, config) {
  // scalastyle:ignore import.grouping
  import SqsMessageFinalizer._

  override def receive: PartialFunction[Any, Unit] = {
    case FinalizeSqsMessage(metadata: SqsMessageMetadata) => {
      val request = new DeleteMessageRequest(queueUrl, metadata.receiptHandle)
      sqsClient.deleteMessageAsync(request, new AsyncHandler[DeleteMessageRequest, Void] {
        override def onError(exception: Exception): Unit = {
          log.error(exception, "Exception thrown while deleting message from SQS")
          die()
        }
        override def onSuccess(request: DeleteMessageRequest, result: Void): Unit = {
          log.info(s"Successfully deleted message with receipt '${metadata.receiptHandle}'")
        }
      })
    }
  }
}
