package com.socrata.decima

import com.socrata.decima.models.DeployForCreate
import org.scalatest.{BeforeAndAfter, ShouldMatchers, WordSpec}

// scalastyle:off multiple.string.literals
// scalastyle:off magic.number

class DeployDAOSpec extends WordSpec with ShouldMatchers with BeforeAndAfter with H2DBSpecUtils {

  import dao.driver.simple._ // scalastyle:ignore import.grouping

  before {
    setUpDb()
  }

  after {
    cleanUpDb()
  }

  "The Deploy DAO" should {
    "create a deploy event" in {
      db.withSession { implicit session: Session =>
        val deploy = dao.createDeploy(DeployForCreate("service",
                                                      "environment",
                                                      "1.1.1",
                                                      "f6b46bd0852a768f9c1b9f3cb0630032f4bfc93f",
                                                      Option("f6b46bd0852a768f9c1b9f3cb0630032f4bfc93f"),
                                                      Option("{ \"this\": \"is a config\" }"),
                                                      "autoprod",
                                                      "an engineer"))
        deploy should be('right)
      }
    }

    "retrieve current deploy status" in {
      db.withSession { implicit session: Session =>
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
      db.withSession { implicit session: Session =>
        val currentDeploys = dao.currentDeployment(Option("staging"), None)
        assert(currentDeploys.length === 2)
        currentDeploys.foreach {
          _.environment should be ("staging")
        }
      }
    }

    "filter current deploy status by environment" in {
      db.withSession { implicit session: Session =>
        val currentDeploys = dao.currentDeployment(None, Option("core"))
        assert(currentDeploys.length === 3)
        currentDeploys.foreach {
          _.service should be ("core")
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
