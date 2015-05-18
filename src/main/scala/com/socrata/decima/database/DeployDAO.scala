package com.socrata.decima.database

import com.socrata.decima.database.tables.DeployTable
import com.socrata.decima.models._
import com.socrata.decima.util.TimeUtils
import grizzled.slf4j.Logging
import org.slf4j.LoggerFactory

class DeployDAO extends DeployTable with Logging {
  self: DatabaseDriver =>

  import driver.simple._ // scalastyle:ignore import.grouping

  val defaultHistoryLimit = 100

  def createDeploy(deploy: DeployForCreate)(implicit session: Session): Either[Exception, Deploy] = {
    session.withTransaction {
      val now = TimeUtils.now
      val newId = (deployTable returning deployTable.map(_.id)) += modelToRowDeploy(deploy)
      val newDeploy = DeployCompiledQueries.lookup(newId).run.headOption
      newDeploy.map(d => Right(rowToModelDeploy(d))).getOrElse(Left(new RuntimeException("Unable to create deploy")))
    }
  }

  def lookup(id: Long)(implicit session: Session): Either[Exception, Deploy] = {
    val deploy = DeployCompiledQueries.lookup(id).run.headOption
    deploy.map(d => Right(rowToModelDeploy(d))).getOrElse(Left(new RuntimeException("Unable to find deploy with id: "
                                                                                    + id)))
  }

  def deploymentHistory(environment: Option[String],
                        service: Option[String],
                        limit: Int = defaultHistoryLimit)(implicit session: Session): Seq[Deploy] = {
    val res = deployTable.filter(row =>
      environment match {
        case Some(e) => row.environment === e
        case None => LiteralColumn(true)
      }).filter(row =>
      service match {
        case Some(s) => row.service === s
        case None => LiteralColumn(true)
      }).sortBy(_.deployedAt.desc).take(limit).run
    res.map(rowToModelDeploy)
  }

  def currentDeployment(environment: Option[String], service: Option[String])(implicit session:Session): Seq[Deploy] = {
    val res = currentDeploymentQuery.list.filter(row => environment match {
      case Some(e) => row.environment == e
      case None => true
    }).filter(row => service match {
      case Some(s) => row.service == s
      case None => true
    })
    res.map(rowToModelDeploy)
  }
}
