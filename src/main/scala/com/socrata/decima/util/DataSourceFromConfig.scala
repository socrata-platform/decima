package com.socrata.decima.util

import com.mchange.v2.c3p0.ComboPooledDataSource
import grizzled.slf4j.Logging
import org.slf4j.LoggerFactory

object DataSourceFromConfig extends Logging {
  def apply(dbConfig: DbConfig): ComboPooledDataSource = {
    val cpds = new ComboPooledDataSource
    cpds.setJdbcUrl(dbConfig.jdbcUrl)
    cpds.setUser(dbConfig.user)
    cpds.setPassword(dbConfig.password)
    logger.info("Created c3p0 connection pool with url: " + cpds.getJdbcUrl)
    cpds
  }
}
