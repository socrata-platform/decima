package com.socrata.decima

import com.socrata.decima.models.{DeployForCreate, Deploy}
import org.slf4j.LoggerFactory

import scala.slick.driver.H2Driver

import com.socrata.decima.database.{DatabaseDriver, DeployDAO}
import org.scalatest.{BeforeAndAfter, WordSpec, ShouldMatchers}

// scalastyle:off multiple.string.literals
// scalastyle:off magic.number

trait ActualH2Driver extends DatabaseDriver {
  val driver = H2Driver
}

class DeployUtil(dao:DeployDAO with DatabaseDriver) {
  import dao.driver.simple._ // scalastyle:ignore
  def populateDeployDb(implicit session:Session): Unit = {
    val deploys = Seq(DeployForCreate("core", "staging", "1.1.1", Option("blah"), "autoprod", "an engineer"),
                      DeployForCreate("core", "rc", "1.1.1", Option("blah"), "autoprod", "an engineer"),
                      DeployForCreate("core", "production", "1.1.1", Option("blah"), "autoprod", "an engineer"),
                      DeployForCreate("core", "staging", "1.1.2", Option("blah"), "autoprod", "an engineer"),
                      DeployForCreate("core", "rc", "1.1.2", Option("blah"), "autoprod", "an engineer"),
                      DeployForCreate("core", "staging", "1.1.3", Option("blah"), "autoprod", "an engineer"),
                      DeployForCreate("frontend", "staging", "1.1.1", Option("blah"), "autoprod", "an engineer"),
                      DeployForCreate("frontend", "rc", "1.1.1", Option("blah"), "autoprod", "an engineer"),
                      DeployForCreate("frontend", "staging", "1.1.2", Option("blah"), "autoprod", "an engineer"),
                      DeployForCreate("frontend", "staging", "1.1.3", Option("blah"), "autoprod", "an engineer"),
                      DeployForCreate("frontend", "staging", "1.1.2", Option("blah"), "autoprod", "an engineer"))
    deploys.foreach(dao.createDeploy)
  }
}

class DeployDAOSpec extends WordSpec with ShouldMatchers with BeforeAndAfter {
  val logger = LoggerFactory.getLogger(getClass)
  val dao = new DeployDAO with ActualH2Driver
  import dao.driver.simple._ // scalastyle:ignore import.grouping
  val db = Database.forURL("jdbc:h2:mem:deploy_dao_test;DATABASE_TO_UPPER=false;DB_CLOSE_DELAY=-1",
                           driver = "org.h2.Driver")
  val util = new DeployUtil(dao)

  before {
    db.withSession { implicit session:Session =>
      dao.deployTable.ddl.create
      util.populateDeployDb
    }
  }

  after {
    db.withSession { implicit session:Session =>
      dao.deployTable.ddl.drop
    }
  }

  "The Deploy DAO" should {
    "create a deploy event" in {
      db.withSession { implicit session:Session =>
        val deploy = dao.createDeploy(DeployForCreate("service",
                                                      "environment",
                                                      "1.1.1",
                                                      Option("f6b46bd0852a768f9c1b9f3cb0630032f4bfc93f"),
                                                      "autoprod",
                                                      "an engineer"))
        deploy should be('right)
      }
    }

    "retrieve current deploy status" in {
      db.withSession { implicit session:Session =>
        val currentDeploys = dao.currentDeployment(None, None)
        currentDeploys.length should be (5)
        currentDeploys.filter(d => d.service == "core" && d.environment == "staging").head.version should be ("1.1.3")
        currentDeploys.filter(d => d.service == "core" && d.environment == "rc").head.version should be ("1.1.2")
        currentDeploys.filter(d => d.service == "core"
                                   && d.environment == "production").head.version should be ("1.1.1")
        currentDeploys.filter(d => d.service == "frontend"
                                   && d.environment == "staging").head.version should be ("1.1.2")
        currentDeploys.filter(d => d.service == "frontend" && d.environment == "rc").head.version should be ("1.1.1")
      }
    }

    "filter current deploy status by service" in {
      db.withSession { implicit session:Session =>
        val currentDeploys = dao.currentDeployment(Option("staging"), None)
        assert(currentDeploys.length === 2)
      }
    }

    "filter current deploy status by environment" in {
      db.withSession { implicit session: Session =>
        val currentDeploys = dao.currentDeployment(None, Option("core"))
        assert(currentDeploys.length === 3)
        currentDeploys.foreach { deploy =>
          deploy.service should be ("core")
        }
      }
    }

    "retrieve deploy history" in {
      db.withSession { implicit session: Session =>
        val deployHistory = dao.deploymentHistory(None, None)
        assert(deployHistory.length === 11)
        val deploy = deployHistory.head
        deploy.service should be ("frontend")
        deploy.version should be ("1.1.2")
      }
    }

    "filter deploy history by service" in {
      db.withSession { implicit session: Session =>
        val deployHistory = dao.deploymentHistory(None, Option("core"))
        assert(deployHistory.length === 6)
        deployHistory.foreach {
          _.service should be ("core")
        }
        val deploy = deployHistory.head
        deploy.service should be ("core")
        deploy.version should be ("1.1.3")
      }
    }

    "filter deploy history by environment" in {
      db.withSession { implicit session: Session =>
        val deployHistory = dao.deploymentHistory(Option("rc"), None)
        assert(deployHistory.length === 3)
        deployHistory.foreach {
          _.environment should be ("rc")
        }
        val deploy = deployHistory.head
        deploy.service should be ("frontend")
        deploy.version should be ("1.1.1")
      }
    }

    "limit deploy history" in {
      db.withSession { implicit session: Session =>
        val deployHistory = dao.deploymentHistory(None, None, 2)
        assert(deployHistory.length === 2)
        val deploy = deployHistory.last
        deploy.service should be ("frontend")
        deploy.version should be ("1.1.3")
      }
    }
  }
}
