package com.socrata.decima.data_access

import com.socrata.decima.database.{DeploymentDAO, DatabaseDriver}
import com.socrata.decima.models._

import scala.slick.driver.PostgresDriver.simple.Database


trait DeploymentAccess {
  def createDeploy(deploy: DeployForCreate): Either[Exception, Deploy]
  def createVerification(verification: VerificationForCreate, deployId: Long): Either[Exception, Verification]
  def deploymentById(deployId: Long): Either[Exception, Deploy]
  def currentDeploymentState(environments: Option[Array[String]], services: Option[Array[String]]): Seq[Deploy]
  def deploymentHistory(environments: Option[Array[String]], services: Option[Array[String]], limit: Int): Seq[Deploy]
  def verificationHistory(deployId: Option[Long]): Seq[Verification]
}

case class DeploymentAccessWithPostgres(db: Database, dao: DeploymentDAO with DatabaseDriver) extends DeploymentAccess {
  override def createDeploy(deploy: DeployForCreate): Either[Exception, Deploy] =
    db.withSession { implicit session =>
      dao.createDeploy(deploy)
    }

  override def currentDeploymentState(environments: Option[Array[String]],
                                      services: Option[Array[String]]): Seq[Deploy] =
    db.withSession { implicit session =>
      dao.currentDeployment(environments, services)
    }

  override def deploymentHistory(environments: Option[Array[String]],
                                 services: Option[Array[String]],
                                 limit: Int): Seq[Deploy] =
    db.withSession { implicit session =>
      dao.deploymentHistory(environments, services, limit)
    }

  override def createVerification(verification: VerificationForCreate, deployId: Long): Either[Exception, Verification] = {
    db.withSession { implicit session =>
      dao.createVerification(verification, deployId)
    }
  }

  override def verificationHistory(deployId: Option[Long]): Seq[Verification] = {
    db.withSession { implicit session =>
      dao.verificationHistory(deployId)
    }
  }

  override def deploymentById(deployId: Long): Either[Exception, Deploy] = {
    db.withSession { implicit session =>
      dao.deploymentById(deployId)
    }
  }
}
