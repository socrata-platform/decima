package com.socrata.decima.actors

import akka.actor.{Actor, ActorLogging, ActorRef}
import com.socrata.decima.actors.SqsActor.SqsMessageMetadata
import com.socrata.decima.actors.SqsMessageFinalizer.FinalizeSqsMessage
import com.socrata.decima.data_access.DeploymentAccess
import com.socrata.decima.data_access.DeploymentAccess.{DeployCreated, DuplicateDeploy}
import com.socrata.decima.models.Deploy

object DeployConsumer {
  case class DeployMessage(deploy: Deploy, metadata: SqsMessageMetadata)
}

class DeployConsumer(deploymentAccess: DeploymentAccess, messageFinalizer: ActorRef) extends Actor with ActorLogging {
  // scalastyle:ignore import.grouping
  import DeployConsumer._

  override def preStart(): Unit = {
    log.info("Starting DeployConsumer")
  }

  override def postStop(): Unit = {
    log.info("Shutting down DeployConsumer")
  }

  def receive: PartialFunction[Any, Unit] = {
    case DeployMessage(deploy, metadata) =>
      log.debug(s"Saving deploy event: $deploy")
      messageFinalizer ! deploymentAccess.createDeploy(deploy).map {
        case DeployCreated(createdDeploy) =>
          log.info(s"Successfully saved deploy $createdDeploy")
          FinalizeSqsMessage(metadata)
        case DuplicateDeploy(service, environment, time) =>
          log.warning(s"Duplicate deploy event for service '$service' in environment '$environment' at '$time'")
          // A duplicate event is typically OK; it is likely a result of
          // re-reading a previously read message
          FinalizeSqsMessage(metadata)
      }.get
  }
}
