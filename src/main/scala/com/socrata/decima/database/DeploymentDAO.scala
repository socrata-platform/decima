package com.socrata.decima.database

import com.socrata.decima.database.tables.VerificationTable
import com.socrata.decima.models._
import com.socrata.decima.util.TimeUtils
import grizzled.slf4j.Logging

class DeploymentDAO extends VerificationTable with Logging {
  self: DatabaseDriver => ()
  import self.driver.simple._ // scalastyle:ignore import.grouping

  private val defaultHistoryLimit = 100

  def createDeploy(deploy: DeployForCreate)(implicit session: Session): Either[Exception, Deploy] = {
    session.withTransaction {
      val newId = (deployTable returning deployTable.map(_.id)) += DeployRow( 0,
                                                                              deploy.service,
                                                                              deploy.environment,
                                                                              deploy.version,
                                                                              deploy.dockerTag,
                                                                              deploy.serviceSha,
                                                                              deploy.dockerSha,
                                                                              deploy.configuration,
                                                                              deploy.deployedBy,
                                                                              deploy.deployMethod,
                                                                              TimeUtils.toSqlTimestamp(TimeUtils.now))
      val newDeploy = DeployCompiledQueries.lookup(newId).run.headOption
      newDeploy.map(d => Right(rowToModelDeploy(d))).getOrElse(Left(new RuntimeException("Unable to create deploy")))
    }
  }

  def createVerification(verification: VerificationForCreate, deployId: Long)
                        (implicit session: Session): Either[Exception, Verification] = {
    session.withTransaction {
      val newId = (verificationTable returning verificationTable.map(_.id)) +=
        VerificationRow(0,
                        verification.status,
                        verification.details,
                        TimeUtils.toSqlTimestamp(TimeUtils.now),
                        deployId)
      val newVerification = VerificationCompiledQueries.lookup(newId).run.headOption
      newVerification.map(v => Right(rowToModelVerification(v)))
        .getOrElse(Left(new RuntimeException("Unable to create verification")))
    }
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

  def currentDeployment(environments: Option[Array[String]],
                        services: Option[Array[String]])
                       (implicit session:Session): Seq[Deploy] = {
    val res = currentDeploymentQuery.list.filter(row => environments match {
      case Some(e) => e.contains(row.environment)
      case None => true
    }).filter(row => services match {
      case Some(s) => s.contains(row.service)
      case None => true
    }).sortBy(row => (row.environment, row.service))
    res.map(rowToModelDeploy)
  }

  def deploymentById(deployId: Long)(implicit session: Session): Either[Exception, Deploy] = {
    DeployCompiledQueries.lookup(deployId).run.headOption.map(d => Right(rowToModelDeploy(d)))
      .getOrElse(Left(new RuntimeException("Deploy not found")))
  }

  def verificationHistory(deployId: Option[Long])(implicit session: Session): Seq[Verification] = {
    session.withTransaction {
      verificationTable.filter(row =>
        deployId match {
          case Some(d) => row.deployId === d
          case None => LiteralColumn(true)
        }).sortBy(_.time.desc).take(defaultHistoryLimit).run.map(rowToModelVerification)
    }
  }

  private def rowToModelDeploy(row: DeployRow)(implicit session: Session): Deploy = {
    Deploy( row.id,
            row.service,
            row.environment,
            row.version,
            row.dockerTag,
            row.serviceSha,
            row.dockerSha,
            row.configuration,
            row.deployedBy,
            row.deployMethod,
            TimeUtils.toJodaDateTime(row.deployedAt),
            getLatestVerificationStatus(row.id))
  }

  private def rowToModelVerification(row: VerificationRow)(implicit session:Session): Verification = {
    Verification( row.id,
                  row.status,
                  row.details,
                  TimeUtils.toJodaDateTime(row.time),
                  row.deployId)
  }

  private def getLatestVerificationStatus(deployId: Long)(implicit session: Session): String = {
    session.withTransaction {
      val query = verificationTable.filter(_.deployId === deployId).sortBy(_.time.desc).map(_.status).take(1)
      query.run.headOption.getOrElse("NOT_FOUND")
    }
  }
}
