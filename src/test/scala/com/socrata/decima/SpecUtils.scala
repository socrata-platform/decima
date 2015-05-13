package com.socrata.decima

import com.socrata.decima.database.{DeployDAO, DatabaseDriver}
import com.socrata.decima.models.DeployForCreate

import scala.slick.driver.H2Driver

// scalastyle:off multiple.string.literals
// scalastyle:off magic.number

trait ActualH2Driver extends DatabaseDriver {
  val driver = H2Driver
}

trait H2DBSpecUtils {

  val dao = new DeployDAO with ActualH2Driver
  import dao.driver.simple._ // scalastyle:ignore import.grouping
  val db = Database.forURL("jdbc:h2:mem:deploy_dao_test;DATABASE_TO_UPPER=false;DB_CLOSE_DELAY=-1",
    driver = "org.h2.Driver")

  def populateDeployDb(implicit session: Session): Unit = {
    val deploys = Seq(DeployForCreate("core", "staging", "1.1.1", "blah", Option("blah"), Option("""{ "this": "is a config" }"""), "autoprod", "an engineer"),
      DeployForCreate("core", "rc", "1.1.1", "blah", Option("blah"), Option("""{ "this": "is a config"}"""), "autoprod", "an engineer"),
      DeployForCreate("core", "production", "1.1.1", "blah", Option("blah"), Option("""{ "this": "is a config"}"""), "autoprod", "an engineer"),
      DeployForCreate("core", "staging", "1.1.2", "blah", Option("blah"), Option("""{ "this": "is a config"}"""), "autoprod", "an engineer"),
      DeployForCreate("core", "rc", "1.1.2", "blah", Option("blah"), Option("""{ "this": "is a config"}"""), "autoprod", "an engineer"),
      DeployForCreate("core", "staging", "1.1.3", "blah", Option("blah"), Option("""{ "this": "is a config"}"""), "autoprod", "an engineer"),
      DeployForCreate("frontend", "staging", "1.1.1", "blah", Option("blah"), Option("""{ "this": "is a config"}"""), "autoprod", "an engineer"),
      DeployForCreate("frontend", "rc", "1.1.1", "blah", Option("blah"), Option("""{ "this": "is a config"}"""), "autoprod", "an engineer"),
      DeployForCreate("frontend", "staging", "1.1.2", "blah", Option("blah"), Option("""{ "this": "is a config"}"""), "autoprod", "an engineer"),
      DeployForCreate("frontend", "staging", "1.1.3", "blah", Option("blah"), Option("""{ "this": "is a config"}"""), "autoprod", "an engineer"),
      DeployForCreate("frontend", "staging", "1.1.2", "blah", Option("blah"), Option("""{ "this": "is a config"}"""), "autoprod", "an engineer"))
    deploys.foreach(dao.createDeploy)
  }

  def setUpDb(): Unit = {
    db.withSession { implicit session: Session =>
      dao.deployTable.ddl.create
      populateDeployDb
    }
  }

  def cleanUpDb(): Unit = {
    db.withSession { implicit session: Session =>
      dao.deployTable.ddl.drop
    }
  }
}
