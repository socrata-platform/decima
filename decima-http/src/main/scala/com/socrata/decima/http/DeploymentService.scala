package com.socrata.decima.http


import com.socrata.decima.data_access.DeploymentAccess.{DuplicateDeploy, DeployCreated}
import com.socrata.decima.data_access._
import com.socrata.decima.models.{Verification, Deploy, AutoprodInfo}
import com.socrata.decima.data_access.S3AccessBase
import com.socrata.decima.util.AutoprodUtils
import org.json4s.JsonAST.{JInt, JField, JObject}
import org.scalatra.Conflict

/**
 * DeployController is responsible for handling requests to /deploy*
 * It contains methods for notifying Decima about deploy events, discovering the version of
 * services in an environment and getting the deploy history.
 * @param deploymentAccess  the database object to use for persistence.
 */
class DeploymentService(deploymentAccess:DeploymentAccess, s3Access: S3AccessBase) extends DecimaStack {

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

    deploymentAccess.currentDeploymentState(environments, services).get
  }

  /**
   * A PUT call to /deploy takes a JSON object representing a deploy event.
   * The call returns the created Deploy object (with current timestamp and new ID)
   */
  put("/") {
    createDeploy(parsedBody.extract[Deploy])
  }

  /**
   * A PUT call to /deploy/autoprod takes a specific JSON object for an autoprod deploy,
   * fetches the `build_info.yml` file and tries to create a Deploy object for it.
   */
  put("/autoprod") {
    val autoprodInfo = parsedBody.extract[AutoprodInfo]
    val buildId = autoprodInfo.buildId match {
      case Some(id) => id
      case None => {
        val builds = s3Access.listBuildPaths(autoprodInfo.project)
        AutoprodUtils.getLatestBuild(builds)
      }
    }
    val buildInfo = s3Access.getBuildInfo(autoprodInfo.project, buildId)
    createDeploy(AutoprodUtils.infoToDeployModel(autoprodInfo, buildInfo))
  }

  /**
   * A GET call to /deploy/{id} gets a single deploy object
   * NOTE: Scalatra executes routes from the bottom UP, so this needs to be above "history" to avoid weird errors
   */
  get(s"/:$idParamKey") {
    val deployId = params(idParamKey).toLong
    deploymentAccess.deploymentById(deployId).get
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

    deploymentAccess.deploymentHistory(environments, services, limit).get
  }

  /**
   * A PUT call to /deploy/{id}/verification allows us to add verification events to individual
   * deploy events. It should be called with the following keys:
   * "status": verification status
   * "details": additional information about the status
   * returns: the new verification information
   */
  put(s"/:$idParamKey/verification") { // scalastyle:ignore multiple.string.literals
    val deployId = params(idParamKey).toLong
    val verification = parsedBody.merge(JObject(
      JField("deployId", JInt(deployId))
    )).extract[Verification]

    val newVerification = deploymentAccess.createVerification(verification).get
    logger.info(s"Created verification event for deploy $deployId: $newVerification")
    newVerification
  }

  /**
   * A GET call to /deploy/{id}/verification returns the log of verification statuses for a deploy event
   * Takes an optional query parameter for
   */
  get(s"/:$idParamKey/verification") {
    val limit = params.get(limitParamKey).map(_.toInt).getOrElse(defaultLimit)
    val deployId = params(idParamKey).toLong
    deploymentAccess.verificationHistory(Option(deployId), limit).get
  }

  private def createDeploy(deploy: Deploy): AnyRef = {
    deploymentAccess.createDeploy(deploy).get match {
      case DeployCreated(createdDeploy) =>
        logger.info("Created deploy event: " + createdDeploy.toString)
        createdDeploy
      case DuplicateDeploy(service, environment, deployedAt) =>
        logger.error(s"Duplicate deploy event for service '$service' in environment '$environment' at '$deployedAt'")
        Conflict(s"Deploy already exists for service '$service' in environment '$environment' at '$deployedAt'")
    }
  }
}
