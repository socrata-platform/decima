package com.socrata.decima.actors

import akka.actor.{ActorLogging, Actor}

import com.socrata.decima.data_access.DeploymentAccess
import com.socrata.decima.data_access.DeploymentAccess.{DeployCreated, DuplicateDeploy}
import com.socrata.decima.models.Deploy

object DeployConsumer {

  trait DeployMessageMetadata
  case class DeployMessage(deploy: Deploy, metadata: DeployMessageMetadata)
  case class DeployProcessedMessage(metadata: DeployMessageMetadata)
}

class DeployConsumer(deploymentAccess: DeploymentAccess) extends Actor with ActorLogging {
  import DeployConsumer._

  override def preStart() = {
    log.info("Starting DeployConsumer")
  }

  override def postStop() = {
    log.info("Shutting down DeployConsumer")
  }

  def receive = {
    case DeployMessage(deploy, metadata) =>
      log.debug(s"Saving deploy event: $deploy")
      sender ! deploymentAccess.createDeploy(deploy).map {
        case DeployCreated(createdDeploy) =>
          log.info(s"Successfully saved deploy $createdDeploy")
          DeployProcessedMessage(metadata)
        case DuplicateDeploy(service, environment, time) =>
          log.warning(s"Duplicate deploy event for service '$service' in environment '$environment' at '$time'")
          // A duplicate event is typically OK; it is likely a result of
          // re-reading a previously read message
          DeployProcessedMessage(metadata)
      }.get
  }
}
