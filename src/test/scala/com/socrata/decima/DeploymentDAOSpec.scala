package com.socrata.decima

import com.socrata.decima.models.DeployForCreate
import org.scalatest.{BeforeAndAfter, ShouldMatchers, WordSpec}

// scalastyle:off multiple.string.literals
// scalastyle:off magic.number

class DeploymentDAOSpec extends WordSpec with ShouldMatchers with BeforeAndAfter with H2DBSpecUtils {

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
                                                      None,
                                                      "f6b46bd0852a768f9c1b9f3cb0630032f4bfc93f",
                                                      Option("f6b46bd0852a768f9c1b9f3cb0630032f4bfc93f"),
                                                      Option("{ \"this\": \"is a config\" }"),
                                                      "autoprod",
                                                      "an engineer"))
        deploy should be('right)
      }
    }

    "create a deploy event for a docker deploy" in {
      db.withSession { implicit session: Session =>
        val deploy = dao.createDeploy(DeployForCreate("dockerservice",
                                                      "staging",
                                                      "1.2.3",
                                                      Option("1.2.3_123_f6b46bd0"),
                                                      "f6b46bd0",
                                                      None,
                                                      Option("blah blah blah"),
                                                      "apps-marathon:deploy",
                                                      "jenkins"))
        deploy should be('right)
        deploy.right.get.dockerTag should be(Option("1.2.3_123_f6b46bd0"))
      }
    }

    "retrieve current deploy status" in {
      db.withSession { implicit session: Session =>
        val currentDeploys = dao.currentDeployment(None, None)
        currentDeploys.length should be (6)
        currentDeploys.filter(d => d.service == "core" && d.environment == "staging").head.version should be ("1.1.3")
        currentDeploys.filter(d => d.service == "core" && d.environment == "rc").head.version should be ("1.1.2")
        currentDeploys.filter(d => d.service == "core"
                                   && d.environment == "production").head.version should be ("1.1.1")
        currentDeploys.filter(d => d.service == "frontend"
                                   && d.environment == "staging").head.version should be ("1.1.2")
        currentDeploys.filter(d => d.service == "frontend" && d.environment == "rc").head.version should be ("1.1.1")
      }
    }

    "filter current deploy status by environment" in {
      db.withSession { implicit session: Session =>
        val currentDeploys = dao.currentDeployment(Option(Array("staging")), None)
        assert(currentDeploys.length === 3)
        currentDeploys.foreach {
          _.environment should be ("staging")
        }
      }
    }

    "filter current deploy status by service" in {
      db.withSession { implicit session: Session =>
        val currentDeploys = dao.currentDeployment(None, Option(Array("core")))
        assert(currentDeploys.length === 3)
        currentDeploys.foreach {
          _.service should be ("core")
        }
      }
    }

    "filter current deploy status by multiple parameters" in {
      db.withSession { implicit session: Session =>
        val currentDeploys = dao.currentDeployment(Option(Array("rc", "staging")), Option(Array("phidippides", "core")))
        assert(currentDeploys.length === 3)
      }
    }

    "retrieve deploy history" in {
      db.withSession { implicit session: Session =>
        val deployHistory = dao.deploymentHistory(None, None)
        assert(deployHistory.length === 12)
        val deploy = deployHistory.head
        deploy.service should be ("phidippides")
        deploy.version should be ("0.13")
      }
    }

    "filter deploy history by service" in {
      db.withSession { implicit session: Session =>
        val deployHistory = dao.deploymentHistory(None, Option(Array("core")))
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
        val deployHistory = dao.deploymentHistory(Option(Array("rc")), None)
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
        deploy.version should be ("1.1.2")
      }
    }

    "filter history by multiple environment parameters" in {
      db.withSession { implicit session: Session =>
        val deployHistory = dao.deploymentHistory(Option(Array("staging", "rc")), None)
        assert(deployHistory.length === 11)
        deployHistory.count(_.environment == "production") should be (0)
      }
    }

    "filter history by multiple service parameters" in {
      db.withSession { implicit session: Session =>
        val deployHistory = dao.deploymentHistory(None, Option(Array("frontend", "phidippides")))
        assert(deployHistory.length === 6)
        deployHistory.count(_.environment == "production") should be (0)
      }
    }

  }
}
