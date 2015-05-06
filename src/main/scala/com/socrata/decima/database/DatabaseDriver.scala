package com.socrata.decima.database

import scala.slick.driver.PostgresDriver

trait DatabaseDriver {
  val driver:PostgresDriver
}

trait ActualPostgresDriver extends DatabaseDriver {
  val driver = PostgresDriver
}
