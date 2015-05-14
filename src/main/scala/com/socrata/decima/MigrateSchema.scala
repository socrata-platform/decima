package com.socrata.decima

import com.mchange.v2.c3p0.ComboPooledDataSource
import com.socrata.decima.database.Migration
import com.socrata.decima.database.Migration.{Migrate, Redo, Undo}
import com.socrata.decima.util.DecimaConfig
import org.slf4j.LoggerFactory

import scala.slick.jdbc.JdbcBackend._

object MigrateSchema extends App {
  val logger = LoggerFactory.getLogger(getClass)

  val cpds = new ComboPooledDataSource
  cpds.setJdbcUrl(DecimaConfig.Db.jdbcUrl)
  cpds.setUser(DecimaConfig.Db.user)
  cpds.setPassword(DecimaConfig.Db.password)
  logger.info("Created c3p0 connection pool")
  logger.info("JDBC URL: " + cpds.getJdbcUrl)
  val db = Database.forDataSource(cpds)

  val numChanges = args.length match {
    case 2 => args(1).toInt
    case 1 => 1
    case _ =>
      throw new IllegalArgumentException(
        s"Incorrect number of arguments - expected 1 or 2 but received ${args.length}")
  }

  val operation = args(0).toLowerCase match {
    case "migrate" => Migrate
    case "undo" => Undo(numChanges)
    case "redo" => Redo(numChanges)
    case _ =>
      throw new IllegalArgumentException(s"Unknown migration operation: '${args(0)}'")
  }

  Migration.migrateDb(cpds.getConnection, operation)

}
