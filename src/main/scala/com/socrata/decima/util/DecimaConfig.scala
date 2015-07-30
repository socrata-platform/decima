package com.socrata.decima.util

import com.typesafe.config.{Config, ConfigFactory}

/**
 * The DecimaConfig object loads configuration settings from
 * the 'reference.conf' file in the project and will merge it with
 * a configuration file specified in -Dconfig.file JAVA arg
 */
object DecimaConfig {
  private val config = ConfigFactory.load

  val app = new AppConfig(config, "decima")
  val db = new DbConfig(config, "c3p0")
  val s3 = new S3Config(config, "s3")
}

class AppConfig(baseConfig: Config, root: String) {
  private val config = baseConfig.getConfig(root)
  val port = config.getInt("port")
}

class DbConfig(baseConfig: Config, root: String) {
  private val config = baseConfig.getConfig(root)
  val jdbcUrl = config.getString("jdbcUrl")
  val user = config.getString("user")
  val password = config.getString("password")
}

class S3Config(baseConfig: Config, root: String) {
  private val config = baseConfig.getConfig(root)
  val accessKeyId = config.getString("accessKeyId")
  val secretAccessKey = config.getString("secretAccessKey")
  val bucketName = config.getString("bucketName")
}
