package com.socrata.decima

import com.typesafe.config.ConfigFactory

/**
 * Created by michaelbrown on 4/23/15.
 */

/**
 * The DecimaConfig object loads configuration settings from
 * the 'reference.conf' file in the project and will merge it with
 * a configuration file specified in -Dconfig.file JAVA arg
 */
object DecimaConfig {

  private val config = ConfigFactory.load
  private val appConfig = config.getConfig("decima")
  private val dbConfig = config.getConfig("c3p0")

  object App {
    val port = appConfig.getInt("port")
  }

  object Db {
    val jdbcUrl = dbConfig.getString("jdbcUrl")
    val user = dbConfig.getString("user")
    val password = dbConfig.getString("password")
  }

}
