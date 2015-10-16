package com.socrata.decima.config

import com.typesafe.config.Config

/**
 * The DecimaConfig object loads configuration settings from
 * the 'reference.conf' file in the project and will merge it with
 * a configuration file specified in -Dconfig.file JAVA arg
 */
class DecimaHttpConfig(config: Config) extends DecimaLibConfig(config) {
  val app = new AppConfig(config.getConfig("decima"))
}

class AppConfig(config: Config) {
  val port = config.getInt("port")
}
