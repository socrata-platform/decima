package com.socrata.decima.data_access

import com.socrata.decima.data_access.DeploymentAccess.DeployResult
import com.socrata.decima.database.{DatabaseDriver, DeploymentDAO}
import com.socrata.decima.models._
import org.joda.time.DateTime
import scala.slick.driver.PostgresDriver.simple.Database
import scala.util.Try

object DeploymentAccess {
  sealed trait DeployResult

  case class DeployCreated(deploy: Deploy) extends DeployResult
  case class DuplicateDeploy(service: String, environment: String, deployTime: DateTime) extends DeployResult
}

trait DeploymentAccess {
  def createDeploy(deploy: Deploy): Try[DeployResult]
  def createVerification(verification: Verification): Try[Verification]
  def deploymentById(deployId: Long): Try[Deploy]
  def currentDeploymentState(environments: Option[Array[String]], services: Option[Array[String]]): Try[Seq[Deploy]]
  def deploymentHistory(environments: Option[Array[String]],
                        services: Option[Array[String]],
                        limit: Int): Try[Seq[Deploy]]
  def verificationHistory(deployId: Option[Long], limit: Int): Try[Seq[Verification]]
}

case class DeploymentAccessWithPostgres(db: Database, dao: DeploymentDAO with DatabaseDriver) extends DeploymentAccess {
  override def createDeploy(deploy: Deploy): Try[DeployResult] =
    db.withSession { implicit session =>
      dao.createDeploy(deploy)
    }

  override def currentDeploymentState(environments: Option[Array[String]],
                                      services: Option[Array[String]]): Try[Seq[Deploy]] =
    db.withSession { implicit session =>
      dao.currentDeployment(environments, services)
    }

  override def deploymentHistory(environments: Option[Array[String]],
                                 services: Option[Array[String]],
                                 limit: Int): Try[Seq[Deploy]] =
    db.withSession { implicit session =>
      dao.deploymentHistory(environments, services, limit)
    }

  override def createVerification(verification: Verification): Try[Verification] = {
    db.withSession { implicit session =>
      dao.createVerification(verification)
    }
  }

  override def verificationHistory(deployId: Option[Long], limit: Int): Try[Seq[Verification]] = {
    db.withSession { implicit session =>
      dao.verificationHistory(deployId, limit)
    }
  }

  override def deploymentById(deployId: Long): Try[Deploy] = {
    db.withSession { implicit session =>
      dao.deploymentById(deployId)
    }
  }
}
