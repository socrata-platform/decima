package com.socrata.decima.services

import com.socrata.decima.data_access._
import com.socrata.decima.models.{VerificationForCreate, DeployForCreate}

/**
 * DeployController is responsible for handling requests to /deploy*
 * It contains methods for notifying Decima about deploy events, discovering the version of
 * services in an environment and getting the deploy history.
 * @param deploymentAccess  the database object to use for persistence.
 */
class DeploymentService(deploymentAccess:DeploymentAccess) extends DecimaStack {

  val serviceParamKey = "service"
  val environmentParamKey = "environment"
  val limitParamKey = "limit"
  val idParamKey = "id"
  val defaultLimit = 100

  // TODO: look up commands in scalatra

  /**
   * A GET call to /deploy will return a JSON array of the latest deploy of each service
   * Optionally, takes the query parameters below and filters the results by them:
   * service: name of the service to return deploy information for
   * environment: environment to return deploy information for
   */
  get("/") { // scalastyle:ignore multiple.string.literals
    val services = params.get(serviceParamKey).map(_.split(","))
    val environments = params.get(environmentParamKey).map(_.split(","))

    deploymentAccess.currentDeploymentState(environments, services)
  }

  /**
   * A PUT call to /deploy takes a JSON object representing a deploy event.
   * The call returns the created Deploy object (with current timestamp and new ID)
   */
  put("/") {
    val deploy = parsedBody.extract[DeployForCreate]

    val createdDeploy = deploymentAccess.createDeploy(deploy)
    logger.info("Created deploy event: " + createdDeploy.toString)
    createdDeploy
  }

  /**
   * A GET call to /deploy/{id} gets a single deploy object
   * NOTE: Scalatra executes routes from the bottom UP, so this needs to be above "history" to avoid weird errors
   */
  get("/:id") {
    val deployId = params(idParamKey).toLong
    deploymentAccess.deploymentById(deployId)
  }

  /**
   * A GET call to /deploy/history returns a JSON array with the last 100 deploys (by default)
   * It can be filtered by similar query parameters as /deploy:
   * service: name of service to return deploy history about
   * environment: environment to return deploy history about
   * limit: override the default number of deploy events to return
   */
  get("/history") {
    val services = params.get(serviceParamKey).map(_.split(","))
    val environments = params.get(environmentParamKey).map(_.split(","))
    val limit = params.getOrElse(limitParamKey, defaultLimit.toString).toInt

    deploymentAccess.deploymentHistory(environments, services, limit)
  }

  /**
   * A PUT call to /deploy/{id}/verification allows us to add verification events to individual
   * deploy events. It should be called with the following keys:
   * "status": verification status
   * "details": additional information about the status
   * returns: the new verification information
   */
  put("/:id/verification") { // scalastyle:ignore multiple.string.literals
    val verification = parsedBody.extract[VerificationForCreate]
    val deployId = params(idParamKey).toLong

    val newVerification = deploymentAccess.createVerification(verification, deployId)
    logger.info(s"Created verification event for deploy $deployId: $newVerification")
    newVerification
  }

  /**
   * A GET call to /deploy/{id}/verification returns the log of verification statuses for a deploy event
   * Takes an optional query parameter for
   */
  get("/:id/verification") {
    val limit = params.getOrElse(limitParamKey, defaultLimit.toString).toInt
    val deployId = params(idParamKey).toLong
    deploymentAccess.verificationHistory(Option(deployId))
  }
}
