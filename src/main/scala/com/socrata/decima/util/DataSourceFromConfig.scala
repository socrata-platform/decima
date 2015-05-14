package com.socrata.decima.util

import com.mchange.v2.c3p0.ComboPooledDataSource
import org.slf4j.LoggerFactory

object DataSourceFromConfig {
  val logger = LoggerFactory.getLogger(getClass)
  def apply(dbConfig: DbConfig): ComboPooledDataSource = {
    val cpds = new ComboPooledDataSource
    cpds.setJdbcUrl(dbConfig.jdbcUrl)
    cpds.setUser(dbConfig.user)
    cpds.setPassword(dbConfig.password)
    logger.info("Created c3p0 connection pool with url: " + cpds.getJdbcUrl)
    cpds
  }
}
