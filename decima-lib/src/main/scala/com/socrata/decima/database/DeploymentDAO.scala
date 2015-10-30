package com.socrata.decima.database

import java.sql.SQLException

import com.socrata.decima.data_access.DeploymentAccess.{DeployCreated, DeployResult, DuplicateDeploy}
import com.socrata.decima.database.tables.VerificationTable
import com.socrata.decima.models._
import com.socrata.decima.util.TimeUtils
import grizzled.slf4j.Logging

import scala.util.Try

object DeploymentDAO {
  // Unique constraint violation code
  // see http://www.postgresql.org/docs/9.4/static/errcodes-appendix.html
  protected val PgUniqueViolationCode = "23505"
}

class DeploymentDAO extends VerificationTable with Logging {
  self: DatabaseDriver => ()

  // scalastyle:ignore import.grouping
  import DeploymentDAO._
  // scalastyle:ignore import.grouping
  import self.driver.simple._

  def createDeploy(deploy: Deploy)(implicit session: Session): Try[DeployResult] = {
    Try {
      session.withTransaction {
        val newId = (deployTable returning deployTable.map(_.id)) += modelToRowDeploy(deploy)
        // Create a verification also, if one is set for the deploy
        deploy.verification match {
          case Some(verification) =>
            createVerification(verification.copy(deployId = newId))
          case None => // Noop
        }
        DeployCreated(
          DeployCompiledQueries.lookup(newId).run.headOption
            .map(rowToModelDeploy)
            .getOrElse(throw new RuntimeException("Unable to create deploy"))
        )
      }
    }.recover {
      case ex: SQLException if ex.getSQLState == PgUniqueViolationCode =>
        DuplicateDeploy(deploy.service, deploy.environment, deploy.deployedAt)
    }
  }

  def createVerification(verification: Verification)
                        (implicit session: Session): Try[Verification] = Try {
    session.withTransaction {
      val newId = (verificationTable returning verificationTable.map(_.id)) += modelToRowVerification(verification)
      VerificationCompiledQueries.lookup(newId).run.headOption
        .map(rowToModelVerification)
        .getOrElse(throw new RuntimeException("Unable to create verification"))
    }
  }

  def deploymentHistory(environments: Option[Array[String]],
                        services: Option[Array[String]],
                        limit: Int)(implicit session: Session): Try[Seq[Deploy]] = Try {
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

  def currentSummary(services: Option[Array[String]])
                       (implicit session:Session): Try[Seq[DeploySummary]] = Try {
    val res = currentDeploymentQuery.list.filter(row => services match {
      case Some(s) => s.contains(row.service)
      case None => true
    }).map(rowToModelDeploy).groupBy(x => getServiceAlias(x.service))
    res.map {
      case (svc, dpls) => {
        val envs = dpls.groupBy(x => getEnvironmentAlias(x.environment))
        val ref = (envs get "rc").map { _.head }
        val refVersion = ref.map { _.version }
	val refDockerTag = ref.flatMap { _.dockerTag }
        val refServiceSha = ref.map { _.serviceSha }

        val envSummaries = envs.mapValues { _.map(deployToEnvironmentDeploySummary(ref, _)) }
        val parity = envSummaries.flatMap { case (k, v) => v.map { _.parityWithReference } }.reduceLeft (_ && _)
        // NOTE :: RC may not exists in this map...
        DeploySummary(svc,
                      parity,
                      refVersion,
                      refDockerTag,
                      refServiceSha,
                      "rc",
                      envSummaries)

      }
    }.toSeq
  }

  def deployToEnvironmentDeploySummary(reference: Option[Deploy], deploy: Deploy): EnvironmentDeploySummary = {
    EnvironmentDeploySummary(deploy.service,
                             getServiceAlias(deploy.service),
                             deploy.environment,
                             reference.map { environmentParityWithReference(_, deploy) }.getOrElse(false),
                             "rc",
                             deploy)
  }

  def environmentParityWithReference(reference: Deploy, deploy: Deploy): Boolean = {
    reference.version == deploy.version && reference.dockerTag == deploy.dockerTag && reference.serviceSha == deploy.serviceSha
  }

  def currentDeployment(environments: Option[Array[String]],
                        services: Option[Array[String]])
                       (implicit session:Session): Try[Seq[Deploy]] = Try {
    val res = currentDeploymentQuery.list.filter(row => environments match {
      case Some(e) => e.contains(row.environment)
      case None => true
    }).filter(row => services match {
      case Some(s) => s.contains(row.service)
      case None => true
    }).sortBy(row => (row.environment, row.service))
    res.map(rowToModelDeploy)
  }

  def deploymentById(deployId: Long)(implicit session: Session): Try[Deploy] = Try {
    DeployCompiledQueries.lookup(deployId).run.headOption
      .map(rowToModelDeploy)
      .getOrElse(throw new RuntimeException("Deploy not found"))
  }

  def verificationHistory(deployId: Option[Long], limit: Int)(implicit session: Session): Try[Seq[Verification]] = Try {
    session.withTransaction {
      verificationTable.filter(row =>
        deployId match {
          case Some(d) => row.deployId === d
          case None => LiteralColumn(true)
        }).sortBy(_.time.desc).take(limit).run.map(rowToModelVerification)
    }
  }

  private def modelToRowDeploy(deploy: Deploy): DeployRow = {
    DeployRow(
      id = deploy.id,
      service = deploy.service,
      environment = deploy.environment,
      version = deploy.version,
      dockerTag = deploy.dockerTag,
      serviceSha = deploy.serviceSha,
      dockerSha = deploy.dockerSha,
      configuration = deploy.configuration,
      deployedBy = deploy.deployedBy,
      deployMethod = deploy.deployMethod,
      deployedAt = TimeUtils.toSqlTimestamp(deploy.deployedAt)
    )
  }

  private def rowToModelDeploy(row: DeployRow)(implicit session: Session): Deploy = {
    Deploy(
      id = row.id,
      service = row.service,
      environment = row.environment,
      version = row.version,
      dockerTag = row.dockerTag,
      serviceSha = row.serviceSha,
      dockerSha = row.dockerSha,
      configuration = row.configuration,
      deployedBy = row.deployedBy,
      deployMethod = row.deployMethod,
      deployedAt = TimeUtils.toJodaDateTime(row.deployedAt),
      verification = getLatestVerificationStatus(row.id)
    )
  }

  private def modelToRowVerification(verification: Verification): VerificationRow = {
    VerificationRow(
      id = verification.id,
      status = verification.status,
      details = verification.details,
      time = TimeUtils.toSqlTimestamp(verification.time),
      deployId = verification.deployId
    )
  }

  private def rowToModelVerification(row: VerificationRow)(implicit session:Session): Verification = {
    Verification(
      id = row.id,
      status = row.status,
      details = row.details,
      time = TimeUtils.toJodaDateTime(row.time),
      deployId = row.deployId
    )
  }

  private def getLatestVerificationStatus(deployId: Long)(implicit session: Session): Option[Verification] = {
    session.withTransaction {
      val query = verificationTable.filter(_.deployId === deployId).sortBy(_.time.desc).take(1)
      query.run.headOption.map(rowToModelVerification)
    }
  }

  private def getEnvironmentAlias(environment: String): String = {
    environment match {
      case _ => environment
    }
  }

  private def getServiceAlias(service: String): String = {
    val soqlServerPattern = """^(soql-server-pg).*""".r
    service match {
      case soqlServerPattern(s) => "soql-server-pg"
      case _ => service
    }
  }
}
