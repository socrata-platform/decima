package com.socrata.decima

import com.typesafe.config.ConfigFactory

import scala.util.Try

/**
 * Created by michaelbrown on 4/23/15.
 */
trait Configuration {

  val config = ConfigFactory.load

  lazy val servicePort = Try(config.getInt("service.port")).getOrElse(8080)

  lazy val dbHost = Try(config.getString("db.host")).toOption.orNull
  lazy val dbPort = Try(config.getString("db.port")).toOption.orNull
  lazy val dbName = Try(config.getString("db.name")).toOption.orNull
  lazy val dbUser = Try(config.getString("db.user")).toOption.orNull
  lazy val dbPassword = Try(config.getString("db.password")).toOption.orNull
}
