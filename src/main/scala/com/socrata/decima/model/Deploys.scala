package com.socrata.decima.model

import java.sql.Timestamp

import scala.slick.driver.JdbcDriver.simple._


/**
 * Created by michaelbrown on 4/23/15.
 */
case class Deploy(id: Option[Int],
                  service: String,
                  environment: String,
                  version: String,
                  git: Option[String],
                  deployedBy: String,
                  deployedAt: Option[Timestamp])

class DeploysTable(tag: Tag) extends Table[Deploy](tag, "deploys") {
  def id = column[Option[Int]]("id", O.AutoInc)
  def service = column[String]("service")
  def environment = column[String]("environment")
  def version = column[String]("version")
  def git = column[Option[String]]("git")
  def deployedBy = column[String]("deployed_by")
  def deployedAt = column[Option[Timestamp]]("deployed_at", O.AutoInc)

  def * = (id, service, environment, version, git, deployedBy, deployedAt) <> (Deploy.tupled, Deploy.unapply)
}
