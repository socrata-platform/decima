package com.socrata.decima

import com.socrata.decima.database.Migration
import com.socrata.decima.database.Migration.{Migrate, Redo, Undo}
import com.socrata.decima.util.{DataSourceFromConfig, DecimaConfig}
import grizzled.slf4j.Logging

import scala.slick.jdbc.JdbcBackend._

object MigrateSchema extends App with Logging {

  val helpMessage =
    """MigrateSchema expects 1 or 2 parameters in the form:
      |   java -cp path/to/decima-assembly.jar com.socrata.decima.MigrateSchema command [number]
      |- commands: migrate, undo, redo
      |- number: number of migrations to undo or redo (ignored for migrate command)
    """.stripMargin

  val cpds = DataSourceFromConfig(DecimaConfig.db)
  val db = Database.forDataSource(cpds)

  val numChanges = args.length match {
    case 2 => args(1).toInt
    case 1 => 1
    case _ =>
      logger.error(helpMessage)
      throw new IllegalArgumentException(
        s"Incorrect number of arguments - expected 1 or 2 but received ${args.length}")
  }

  val operation = args(0).toLowerCase match {
    case "migrate" => Migrate
    case "undo" => Undo(numChanges)
    case "redo" => Redo(numChanges)
    case _ =>
      logger.error(helpMessage)
      throw new IllegalArgumentException(s"Unknown migration operation: '${args(0)}'")
  }

  Migration.migrateDb(cpds.getConnection, operation)
}
