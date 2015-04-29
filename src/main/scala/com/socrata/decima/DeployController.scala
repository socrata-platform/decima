package com.socrata.decima

import com.socrata.decima.model.{Deploy, DeploysTable}
import org.json4s.{JValue, DefaultFormats, Formats}
import org.scalatra.ScalatraServlet
import org.scalatra.json._
import org.slf4j.LoggerFactory

import scala.slick.driver.JdbcDriver.simple._
import scala.slick.jdbc.JdbcBackend.DatabaseDef
import scala.slick.jdbc.{GetResult, StaticQuery => Q}
import scala.slick.lifted.TableQuery

/**
 * DeployController is responsible for handling requests to /deploy and /deploy/history
 * It contains methods for notifying Decima about deploy events, discovering the version of
 * services in an environment and getting the deploy history.
 * @param db  the database object to use for persistence.
 */
class DeployController(db: DatabaseDef) extends ScalatraServlet with JacksonJsonSupport {

  val logger = LoggerFactory.getLogger(getClass)
  val deploys = TableQuery[DeploysTable]

  val serviceParamKey = "service"
  val environmentParamKey = "environment"
  val limitParamKey = "limit"

  // TODO: shouldn't this be put in the Deploy model somehow
  implicit val getDeployResult = GetResult(r => Deploy(r.<<, r.<<, r.<<, r.<<, r.<<?, r.<<, r.<<))
  val currentDeployment = Q.queryNA[Deploy]("""
    select a.id, a.service, a.environment, b.version, b.git, b.deployed_by, b.deployed_at
      from (
        select distinct deploys.service, deploys.environment, max(deploys.id) as id
        from deploys
        group by deploys.service, deploys.environment) a,
      deploys b
      where a.id = b.id
    """)

  // TODO: put this into a trait or base class
  // Sets up automatic case class to JSON output serialization, required by
  // the JValueResult trait.
  protected implicit val jsonFormats: Formats = DefaultFormats
  override protected def transformRequestBody(body: JValue): JValue = body.camelizeKeys
  override protected def transformResponseBody(body: JValue): JValue = body.underscoreKeys

  // Before every action runs, set the content type to be in JSON format.
  before() {
    contentType = formats("json")
  }

  // TODO: add an error handler (this should go in base class, too)
  error {
    case e: Exception => // define behavior
  }

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

    db.withSession { implicit session =>
      currentDeployment.list.filter(row => serviceName match {
        case Some(s) => row.service == s
        case None => true
      }).filter(row => environmentName match {
        case Some(e) => row.environment == e
        case None => true
      }).sortBy(d  => (d.environment, d.service))
    }
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

    val deploy = parsedBody.extract[Deploy]
    logger.info(deploys.insertStatement)
    db.withSession { implicit session =>
      deploys returning deploys += deploy
    }
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
    val limit = params.getOrElse(limitParamKey, "100").toInt // TODO: move 100 to const

    db.withSession { implicit session =>

      val query = deploys.filter(row => serviceName match {
        case Some(s) => row.service === s
        case None => LiteralColumn(true)
      }).filter(row => environmentName match {
        case Some(e) => row.environment === e
        case None => LiteralColumn(true)
      }).sortBy(row => row.deployedAt.desc).take(limit)

      logger.info("Generated SQL for base Deploys query:\n" + query.selectStatement)
      query.run
    }

  }
}
