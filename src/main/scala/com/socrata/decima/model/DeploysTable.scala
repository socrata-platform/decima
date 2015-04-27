package com.socrata.decima.lachesis.model

import java.sql.Timestamp

import scala.slick.driver.JdbcDriver.simple._
import scala.slick.jdbc.GetResult


/**
 * Created by michaelbrown on 4/23/15.
 */
case class Deploy(id: Option[Int], service: String, environment: String, version: String, git: Option[String], timestamp: Option[Timestamp])

//class DeploysTable(tag: Tag) extends Table[(String, String, String, Option[String], Timestamp)](tag, "deploys") {
class DeploysTable(tag: Tag) extends Table[Deploy](tag, "deploys") {
  def id = column[Option[Int]]("id", O.AutoInc)
  def service = column[String]("service")
  def environment = column[String]("environment")
  def version = column[String]("version")
  def git = column[Option[String]]("git")
  def timestamp = column[Option[Timestamp]]("timestamp", O.AutoInc)

  def * = (id, service, environment, version, git, timestamp) <> (Deploy.tupled, Deploy.unapply)
}

