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

  def deploymentHistory(environments: Option[Array[String]],
                        services: Option[Array[String]],
                        limit: Int = defaultHistoryLimit)(implicit session: Session): Seq[Deploy] = {
    val query = deployTable.filter(row => environments match {
      case Some(e) => row.environment inSet e
      case None => LiteralColumn(true)
    }).filter(row => services match {
      case Some(s) => row.service inSet s
      case None => LiteralColumn(true)
    }).sortBy(_.deployedAt.desc).take(limit)
    val res = query.run
    res.map(rowToModelDeploy)
  }

  def currentDeployment(environments: Option[Array[String]], services: Option[Array[String]])(implicit session:Session): Seq[Deploy] = {
    val res = currentDeploymentQuery.list.filter(row => environments match {
      case Some(e) => e.contains(row.environment)
      case None => true
    }).filter(row => services match {
      case Some(s) => s.contains(row.service)
      case None => true
    }).sortBy(row => (row.environment, row.service))
    res.map(rowToModelDeploy)
  }
}
