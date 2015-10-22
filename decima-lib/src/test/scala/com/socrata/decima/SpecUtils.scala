package com.socrata.decima

import com.socrata.decima.database.{DatabaseDriver, DeploymentDAO}
import com.socrata.decima.models.Deploy
import org.joda.time.DateTime

import scala.slick.driver.H2Driver

trait ActualH2Driver extends DatabaseDriver {
  val driver = H2Driver
}

trait H2DBSpecUtils {
  val dao = new DeploymentDAO with ActualH2Driver
  import dao.driver.simple._ // scalastyle:ignore import.grouping
  val db = Database.forURL(s"jdbc:h2:mem:${getClass.getSimpleName};DATABASE_TO_UPPER=false;DB_CLOSE_DELAY=-1",
    driver = "org.h2.Driver")

  def populateDeployDb(implicit session: Session): Unit = {
    // scalastyle:off line.size.limit
    val deploys = Seq(
      Deploy("core", "staging", "1.1.1", None, "blah", Option("blah"), Option("""{ "this": "is a config" }"""), "autoprod", "an engineer"),
      Deploy("core", "rc", "1.1.1", None, "blah", Option("blah"), Option("""{ "this": "is a config"}"""), "autoprod", "an engineer"),
      Deploy("core", "production", "1.1.1", None, "blah", Option("blah"), Option("""{ "this": "is a config"}"""), "autoprod", "an engineer"),
      Deploy("core", "staging", "1.1.2", None, "blah", Option("blah"), Option("""{ "this": "is a config"}"""), "autoprod", "an engineer"),
      Deploy("core", "rc", "1.1.2", None, "blah", Option("blah"), Option("""{ "this": "is a config"}"""), "autoprod", "an engineer"),
      Deploy("core", "staging", "1.1.3", None, "blah", Option("blah"), Option("""{ "this": "is a config"}"""), "autoprod", "an engineer"),
      Deploy("frontend", "staging", "1.1.1", None, "blah", Option("blah"), Option("""{ "this": "is a config"}"""), "autoprod", "an engineer"),
      Deploy("frontend", "rc", "1.1.1", None, "blah", Option("blah"), Option("""{ "this": "is a config"}"""), "autoprod", "an engineer"),
      Deploy("frontend", "staging", "1.1.2", None, "blah", Option("blah"), Option("""{ "this": "is a config"}"""), "autoprod", "an engineer"),
      Deploy("frontend", "staging", "1.1.3", None, "blah", Option("blah"), Option("""{ "this": "is a config"}"""), "autoprod", "an engineer"),
      Deploy("frontend", "staging", "1.1.2", None, "blah", Option("blah"), Option("""{ "this": "is a config"}"""), "autoprod", "an engineer"),
      Deploy("phidippides", "staging", "0.13", None, "blah", Option("blah"), Option("""{ "this": "is a config"}"""), "autoprod", "an engineer")
    )
    // scalastyle:on line.size.limit
    // Explicitly set increasing time to avoid any race conditions in test due to object creation
    val startTime = DateTime.now
    deploys.zipWithIndex.foreach { case (deploy, idx) =>
      dao.createDeploy(deploy.copy(deployedAt = startTime.plusSeconds(idx)))
    }
  }

  def setUpDb(): Unit = {
    db.withSession { implicit session: Session =>
      dao.deployTable.ddl.create
      dao.verificationTable.ddl.create
      populateDeployDb
    }
  }

  def cleanUpDb(): Unit = {
    db.withSession { implicit session: Session =>
      dao.verificationTable.ddl.drop
      dao.deployTable.ddl.drop
    }
  }
}
