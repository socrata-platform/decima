package com.socrata.decima.data_access

import com.socrata.decima.database.{DatabaseDriver, DeployDAO}
import com.socrata.decima.models._
import slick.driver.PostgresDriver.simple.Database


trait DeployAccess {
  def createDeploy(deploy:DeployForCreate):Either[Exception, Deploy]
  def currentDeploymentState(environment:Option[String], service:Option[String]):Seq[Deploy]
  def deploymentHistory(environment:Option[String], service:Option[String]):Seq[Deploy]
}

case class DeployAccessWithPostgres(db:Database, dao:DeployDAO with DatabaseDriver) extends DeployAccess {
  override def createDeploy(deploy: DeployForCreate): Either[Exception, Deploy] = {
    db.withSession { implicit session =>
      dao.createDeploy(deploy)
    }
  }

  override def currentDeploymentState(environment: Option[String], service: Option[String]): Seq[Deploy] = {
    db.withSession { implicit session =>
      dao.currentDeployment
    }
  }

  override def deploymentHistory(environment: Option[String], service: Option[String]): Seq[Deploy] = {
    db.withSession { implicit session =>
      dao.deploymentHistory(10)
    }
  }
}
