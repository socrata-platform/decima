package com.socrata.decima

import com.typesafe.config.ConfigFactory

/**
 * Created by michaelbrown on 4/23/15.
 */
object DecimaConfig {

  private val config = ConfigFactory.load
  private val appConfig = config.getConfig("decima")
  private val dbConfig = config.getConfig("c3p0")

  object app {
    val port = appConfig.getInt("port")
  }

  object db {
    val jdbcUrl = dbConfig.getString("jdbcUrl")
    val user = dbConfig.getString("user")
    val password = dbConfig.getString("password")
  }

}
