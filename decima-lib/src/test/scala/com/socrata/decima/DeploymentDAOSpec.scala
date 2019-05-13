package com.socrata.decima

import com.socrata.decima.data_access.DeploymentAccess.DeployCreated
import com.socrata.decima.models._
import org.scalatest.{BeforeAndAfter, Matchers, WordSpec}

class DeploymentDAOSpec extends WordSpec with Matchers with BeforeAndAfter with H2DBSpecUtils {
  import dao.driver.simple._ // scalastyle:ignore import.grouping

  private val defaultLimit = 100

  before {
    setUpDb()
  }

  after {
    cleanUpDb()
  }

  private def createDeploy(deploy: Deploy)(onSuccess: Deploy => Unit = (_) => {})(implicit session: Session): Unit = {
    dao.createDeploy(deploy).get match {
      case DeployCreated(createdDeploy) => onSuccess(createdDeploy)
      case other => fail(s"Unexpected DeployResult: $other")
    }
  }

  "The Deployment DAO" should {
    "create a deploy event" in {
      db.withSession { implicit session: Session =>
        createDeploy(
          Deploy(
            "service",
            "environment",
            "1.1.1",
            None,
            "f6b46bd0852a768f9c1b9f3cb0630032f4bfc93f",
            Option("f6b46bd0852a768f9c1b9f3cb0630032f4bfc93f"),
            Option("{ \"this\": \"is a config\" }"),
            "autoprod",
            "an engineer"
          )
        )()
      }
    }

    "create a deploy event for a docker deploy" in {
      db.withSession { implicit session: Session =>
        createDeploy(
          Deploy(
            "dockerservice",
            "staging",
            "1.2.3",
            Option("1.2.3_123_f6b46bd0"),
            "f6b46bd0",
            None,
            Option("blah blah blah"),
            "apps-marathon:deploy",
            "jenkins"
          )
        ) { deploy =>
          deploy.dockerTag should be(Some("1.2.3_123_f6b46bd0"))
        }
      }
    }

    "create a verification event for a deployment" in {
      db.withSession { implicit session: Session =>
        createDeploy(
          Deploy(
            "service",
            "staging",
            "1.2.3-SNAPSHOT",
            Option("1.2.3-SNAPSHOT_555_2e8sd09f"),
            "blahblah",
            None,
            Option("this configuration"),
            "autoprod:deploy",
            "a user"
          )
        ) { deploy =>
          val verification = dao.createVerification(
            Verification(
              status = "VERIFIED",
              deployId = deploy.id,
              details = Some("all instances are in sync")
            )
          )
          verification should be('success)
          val verifiedDeploy = dao.deploymentById(deploy.id).get.verification.get
          verifiedDeploy.status should be("VERIFIED")
          verifiedDeploy.deployId should be(deploy.id)
          verifiedDeploy.details should be(Some("all instances are in sync"))
        }
      }
    }

    "have the verified field set to NONE initially by default" in {
      db.withSession { implicit session: Session =>
        createDeploy(
          Deploy(
            "service",
            "staging",
            "1.2.3-SNAPSHOT",
            Option("1.2.3-SNAPSHOT_555_2e8sd09f"),
            "blahblah",
            None,
            Option("this configuration"),
            "autoprod:deploy",
            "a user"
          )
        ) { deploy =>
          deploy.verification should be(None)
        }
      }
    }

    "accept a verification when creating a deploy" in {
      db.withSession { implicit session: Session =>
        val verification = Verification("Test-verification", 0, details = Some("foo, bar"))
        createDeploy(
          Deploy(
            "service",
            "staging",
            "1.2.3-SNAPSHOT",
            Option("1.2.3-SNAPSHOT_555_2e8sd09f"),
            "blahblah",
            None,
            Option("this configuration"),
            "autoprod:deploy",
            "a user",
            verification = Some(verification)
          )
        ) { deploy =>
          val actualVerification = deploy.verification.get
          actualVerification.deployId should be(deploy.id)
          actualVerification.time should be(verification.time)
          actualVerification.status should be(verification.status)
          actualVerification.details should be(verification.details)
        }
      }
    }

    "retrieve the verification events for a deploy with the most recent first" in {
      db.withSession { implicit session: Session =>
        createDeploy(
          Deploy(
            "service",
            "staging",
            "1.2.3-SNAPSHOT",
            Option("1.2.3-SNAPSHOT_555_2e8sd09f"),
            "blahblah",
            None,
            Option("this configuration"),
            "autoprod:deploy",
            "a user"
          )
        ) { deploy =>
          0.to(4).foreach( idx =>
            dao.createVerification(
              Verification(
                "NOT_VERIFIED",
                deployId =  deploy.id,
                details = Option(s"test verification event $idx")
              )
            )
          )
          val verified = dao.createVerification(
            Verification(
              "VERIFIED",
              deployId = deploy.id,
              details = Option("verified at last!")
            )
          ).get
          val verifications = dao.verificationHistory(Option(deploy.id), defaultLimit).get
          verifications.size should be(6)
          verifications.head should be(verified)
        }
      }
    }

    "respect limits for verification history" in {
      val limit = 3
      db.withSession { implicit session: Session =>
        createDeploy(
          Deploy(
            "service",
            "staging",
            "1.2.3-SNAPSHOT",
            Option("1.2.3-SNAPSHOT_555_2e8sd09f"),
            "blahblah",
            None,
            Option("this configuration"),
            "autoprod:deploy",
            "a user"
          )
        ) { deploy =>
          0.to(limit * 2).foreach( idx =>
            dao.createVerification(
              Verification(
                status = "NOT_VERIFIED",
                deployId = deploy.id,
                details = Option(s"test verification event $idx")
              )
            )
          )
          val verified = dao.createVerification(
            Verification(
              status = "VERIFIED",
              deployId = deploy.id,
              details = Option("verified at last!")
            )
          ).get
          val verifications = dao.verificationHistory(Option(deploy.id), limit).get
          verifications.size should be(limit)
        }
      }
    }

    "retrieve current deploy status" in {
      db.withSession { implicit session: Session =>
        val currentDeploys = dao.currentDeployment(None, None).get
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
        val currentDeploys = dao.currentDeployment(Option(Array("staging")), None).get
        assert(currentDeploys.length === 3)
        currentDeploys.foreach {
          _.environment should be ("staging")
        }
      }
    }

    "filter current deploy status by service" in {
      db.withSession { implicit session: Session =>
        val currentDeploys = dao.currentDeployment(None, Option(Array("core"))).get
        assert(currentDeploys.length === 3)
        currentDeploys.foreach {
          _.service should be ("core")
        }
      }
    }

    "filter current deploy status by multiple parameters" in {
      db.withSession { implicit session: Session =>
        val currentDeploys = dao.currentDeployment(Option(Array("rc", "staging")), Option(Array("phidippides", "core"))).get
        assert(currentDeploys.length === 3)
      }
    }

    "retrieve deploy history" in {
      db.withSession { implicit session: Session =>
        val deployHistory = dao.deploymentHistory(None, None, defaultLimit).get
        assert(deployHistory.length === 12)
        val deploy = deployHistory.head
        deploy.service should be ("phidippides")
        deploy.version should be ("0.13")
      }
    }

    "filter deploy history by service" in {
      db.withSession { implicit session: Session =>
        val deployHistory = dao.deploymentHistory(None, Option(Array("core")), defaultLimit).get
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
        val deployHistory = dao.deploymentHistory(Option(Array("rc")), None, defaultLimit).get
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
        val deployHistory = dao.deploymentHistory(None, None, 2).get
        assert(deployHistory.length === 2)
        val deploy = deployHistory.last
        deploy.service should be ("frontend")
        deploy.version should be ("1.1.2")
      }
    }

    "filter history by multiple environment parameters" in {
      db.withSession { implicit session: Session =>
        val deployHistory = dao.deploymentHistory(Option(Array("staging", "rc")), None, defaultLimit).get
        assert(deployHistory.length === 11)
        deployHistory.count(_.environment == "production") should be (0)
      }
    }

    "filter history by multiple service parameters" in {
      db.withSession { implicit session: Session =>
        val deployHistory = dao.deploymentHistory(None, Option(Array("frontend", "core")), defaultLimit).get
        assert(deployHistory.length === 11)
        deployHistory.count(_.environment == "production") should be (1)
      }
    }

    "retrieve current deploy summary" in {
      setupSoqlParityTest()
      db.withSession { implicit session: Session =>
        val deploySummary = dao.currentSummary(None).get
        deploySummary.count(_.serviceAlias == "core") should be (1)
        deploySummary.count(_.serviceAlias == "frontend") should be (1)
        deploySummary.count(_.serviceAlias == "phidippides") should be (0)
        deploySummary.count(_.serviceAlias == "soql-server-pg") should be (1)
        deploySummary.length should be (3)
        deploySummary.foreach { x =>
          x.serviceAlias match {
            case "core" =>  x.parity should be (false)
            case "frontend" =>  x.parity should be (true)
            case "soql-server-pg" =>  x.parity should be (true)
          }
        }
      }
    }

    "filter current deploy summary by service" in {
      setupSoqlParityTest()
      db.withSession { implicit session: Session =>
        val deploySummary = dao.currentSummary(Option(Array("soql-server-pg"))).get
        deploySummary.length should be (1)
        deploySummary.count(_.serviceAlias == "soql-server-pg") should be (1)
        deploySummary.head.parity should be (true)
      }
    }
  }
}
