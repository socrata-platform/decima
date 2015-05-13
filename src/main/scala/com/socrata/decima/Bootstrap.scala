package com.socrata.decima

import com.socrata.decima.util.DecimaConfig
import org.slf4j.LoggerFactory

import scala.slick.driver.PostgresDriver
import scala.slick.jdbc.JdbcBackend._

import com.mchange.v2.c3p0.ComboPooledDataSource
import com.socrata.decima.database.DatabaseDriver
import com.socrata.decima.database.tables._

object Bootstrap extends App {
  val logger = LoggerFactory.getLogger(getClass)

  val cpds = new ComboPooledDataSource
  cpds.setJdbcUrl(DecimaConfig.Db.jdbcUrl)
  cpds.setUser(DecimaConfig.Db.user)
  cpds.setPassword(DecimaConfig.Db.password)
  logger.info("Created c3p0 connection pool")
  logger.info("JDBC URL: " + cpds.getJdbcUrl)
  val db = Database.forDataSource(cpds)

  class Tables() extends DeployTable with DatabaseDriver { val driver = PostgresDriver }

  val a = new Tables()
  import a.driver.simple._ // scalastyle:ignore import.grouping

  db.withSession{ implicit session:Session =>
    a.deployTable.ddl.createStatements.foreach(logger.info)
    a.deployTable.ddl.create
  }
}
