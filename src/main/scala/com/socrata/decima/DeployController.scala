package com.socrata.decima

// JSON-related libraries

import org.json4s.{DefaultFormats, Formats}
import org.slf4j.LoggerFactory

import scala.slick.jdbc.JdbcBackend.DatabaseDef
import scala.slick.lifted.TableQuery
import scala.slick.driver.JdbcDriver.simple._
import scala.slick.jdbc.{GetResult, StaticQuery => Q}


// JSON handling support from Scalatra
import org.scalatra.json._

import org.scalatra.ScalatraServlet

import com.socrata.decima.model.{Deploy, DeploysTable}

/**
 * Created by michaelbrown on 4/22/15.
 */
class DeployController(db: DatabaseDef) extends ScalatraServlet with JacksonJsonSupport {

  val logger = LoggerFactory.getLogger(getClass)
  val deploys = TableQuery[DeploysTable]

//  val latestDeploys = deploys.groupBy(d => d.service)
//    .map { case (service, deploy) => (service, deploy.map())}
//
//  val currentDeployment = for {
//    a <- latestDeploys
//    b <- deploys if a._1 === b.id
//  } yield (a._1, a._2, a._3, b.version, b.git, b.timestamp)

  // TODO: shouldn't this be put in the Deploy model somehow
  implicit val getDeployResult = GetResult(r => Deploy(r.<<, r.<<, r.<<, r.<<, r.<<?, r.<<))
  val currentDeployment = Q.queryNA[Deploy]("""
    select a.id, a.service, a.environment, b.version, b.git, b.timestamp
      from (
      select distinct deploys.service, deploys.environment, max(deploys.id) as id
      from deploys
      group by deploys.service, deploys.environment) a, deploys b
      where a.id = b.id
    """)

  // Sets up automatic case class to JSON output serialization, required by
  // the JValueResult trait.
  protected implicit val jsonFormats: Formats = DefaultFormats

  // Before every action runs, set the content type to be in JSON format.
  before() {
    contentType = formats("json")
  }

  get("/") {
    logger.info("/ get request with params: " + params.toString)

    val serviceName = params.getOrElse("service", null)
    val environmentName = params.getOrElse("environment", null)

    db.withSession { implicit session =>

      currentDeployment.list.filter(row => serviceName match {
        case _: String => row.service == serviceName
        case null => true
      }).filter(row => environmentName match {
        case _: String => row.environment == environmentName
        case null => true
      }).sortBy(d  => (d.environment, d.service))
    }

    //      val query = deploys.filter(row => serviceName match {
    //        case _: String => row.service === serviceName
    //        case null => LiteralColumn(true)
    //      }).filter(row => environmentName match {
    //        case _: String => row.environment === environmentName
    //        case null => LiteralColumn(true)
    //      })
    //
    //      // Print the SQL for the Deploys query
    //      logger.info("Generated SQL for base Deploys query:\n" + query.selectStatement)
    //      query.run
  }

  get("/history") {
    logger.info("/history get request with params:" + params.toString)
    val serviceName = params.getOrElse("service", null)
    val environmentName = params.getOrElse("environment", null)
    val limit = params.getOrElse("limit", "5").toInt

    db.withSession { implicit session =>

      val query = deploys.filter(row => serviceName match {
        case _: String => row.service === serviceName
        case null => LiteralColumn(true)
      }).filter(row => environmentName match {
        case _: String => row.environment === environmentName
        case null => LiteralColumn(true)
      }).sortBy(row => row.timestamp.desc).take(limit)

      // Print the SQL for the Deploys query
      logger.info("Generated SQL for base Deploys query:\n" + query.selectStatement)
      query.run
    }

  }

  put("/") {
    logger.info("put request")
    logger.info(parsedBody.toString)
    val deploy = parsedBody.extract[Deploy]

    logger.info(deploys.insertStatement)

    db.withSession { implicit session =>
      deploys returning deploys += deploy
    }
  }

//  // update a deploy event's status
//  put("/:id") {
//    val id = params.getOrElse("id", halt(400)).toInt
//    logger.info("update deploy event with id: " + id)
//    logger.info("with body: " + parsedBody)
//    val body = parsedBody.extract[Map[String, String]]
//    val status = body.getOrElse("status", halt(400))
//    db.withSession { implicit session =>
//      deploys filter (_.id === id) map (d => d.status) update status
//    }
//    body
//  }
}
