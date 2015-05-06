package com.socrata.decima.services

import com.socrata.decima.data_access.DeployAccess
import com.socrata.decima.models.DeployForCreate

/**
 * DeployController is responsible for handling requests to /deploy and /deploy/history
 * It contains methods for notifying Decima about deploy events, discovering the version of
 * services in an environment and getting the deploy history.
 * @param deployAccess  the database object to use for persistence.
 */
class DeployService(deployAccess:DeployAccess) extends DecimaStack {

  val serviceParamKey = "service"
  val environmentParamKey = "environment"
  val limitParamKey = "limit"

  // TODO: look up commands in scalatra

  /**
   * A GET call to /deploy will return a JSON array of the latest deploy of each service
   * Optionally, takes the query parameters below and filters the results by them:
   * service: name of the service to return deploy information for
   * environment: environment to return deploy information for
   */
  get("/") { // scalastyle:ignore multiple.string.literals
    logger.info("/deploy get request with params: " + params.toString)
    val serviceName = params.get(serviceParamKey)
    val environmentName = params.get(environmentParamKey)

    val deploys = deployAccess.currentDeploymentState(environmentName, serviceName)
    println(deploys.head)
    deploys
  }

  /**
   * A PUT call to /deploy takes a JSON object with the following keys:
   * "service": name of service being deployed
   * "environment": the environment being deployed to
   * "version": version being deployed
   * "git": the git SHA of the deploy
   */
  put("/") {
    logger.info("/deploy put request")
    logger.info(parsedBody.toString)

    val deploy = parsedBody.extract[DeployForCreate]
    deployAccess.createDeploy(deploy)
  }

  /**
   * A GET call to /deploy/history returns a JSON array with the last 100 deploys (by default)
   * It can be filtered by similar query parameters as /deploy:
   * service: name of service to return deploy history about
   * environment: environment to return deploy history about
   * limit: override the default number of deploy events to return
   */
  get("/history") {
    logger.info("/deploy/history get request with params:" + params.toString)

    val serviceName = params.get(serviceParamKey)
    val environmentName = params.get(environmentParamKey)
//    val limit = params.getOrElse(limitParamKey, "100").toInt // TODO: move 100 to const

    deployAccess.deploymentHistory(environmentName, serviceName)
  }
}
