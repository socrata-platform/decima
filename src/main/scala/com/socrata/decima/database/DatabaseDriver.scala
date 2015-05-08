package com.socrata.decima.database

import scala.slick.driver.{JdbcDriver, PostgresDriver}

trait DatabaseDriver {
  val driver: JdbcDriver
}

trait ActualPostgresDriver extends DatabaseDriver {
  val driver = PostgresDriver
}
