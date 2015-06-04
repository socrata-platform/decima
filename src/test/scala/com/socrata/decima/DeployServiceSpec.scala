package com.socrata.decima

import com.socrata.decima.data_access.DeployAccessWithPostgres
import com.socrata.decima.models.{DeployForCreate, Deploy}
import com.socrata.decima.services.{ErrorMessage, DeployService}
import org.scalatest._
import org.scalatra.test.scalatest.ScalatraSuite
import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.json4s.jackson.Serialization.write

// scalastyle:off multiple.string.literals
// scalastyle:off magic.number

class DeployServiceSpec extends ScalatraSuite with WordSpecLike with BeforeAndAfter
                        with ShouldMatchers with H2DBSpecUtils {

  import dao.driver.simple._ // scalastyle:ignore import.grouping

  implicit val formats = DefaultFormats ++ org.json4s.ext.JodaTimeSerializers.all
  val deployAccess = new DeployAccessWithPostgres(db, dao)
  addServlet(new DeployService(deployAccess), "/deploy/*")

  def parseResponse(body: String): Seq[Deploy] = parse(body).camelizeKeys.extract[Seq[Deploy]]

  before {
    setUpDb
  }

  after {
    cleanUpDb
  }

  "The Deploy Service /deploy GET" should {
    "return 200 on a simple get" in {
      get("/deploy") {
        status should be (200)
      }
    }

    "return the correct number of deployed services" in {
      get("/deploy") {
        val deploys = parseResponse(response.body)
        deploys.length should be (6)
      }
    }

    "allow filtering based on environment" in {
      get("/deploy?environment=production") {
        val deploys = parseResponse(response.body)
        deploys.length should be (1)
      }
    }

    "allow filtering based on service" in {
      get("/deploy?service=frontend") {
        val deploys = parseResponse(response.body)
        deploys.length should be (2)
      }
    }
  }

  "The Deploy Service /deploy PUT" should {
    "allow creation of a valid Deploy object" in {
      val newDeploy = DeployForCreate("phidippides",
                                      "rc",
                                      "1.1.10",
                                      None,
                                      "aalsdfkjasldkfjaslkdfhkasdjhfkdgdfdfgdf",
                                      Option("asdfoqiweurqweorasdflkajsdowqieurodisjf"),
                                      Option("{\"this\": \"is a config\"}"),
                                      "an engineer",
                                      "autoprod")
      val body = write(parse(write(newDeploy)).underscoreKeys)
      put("/deploy", body) {
        status should be (200)
        val deploy = parse(response.body).camelizeKeys.extract[Deploy]
        db.withSession { implicit session: Session =>
          dao.lookup(deploy.id) should be ('right)
        }
      }
    }

    "allow creation of a valid Deploy object with a large config blob" in {
      val newDeploy = DeployForCreate("phidippides",
        "rc",
        "1.1.10",
        None,
        "aalsdfkjasldkfjaslkdfhkasdjhfkdgdfdfgdf",
        Option("asdfoqiweurqweorasdflkajsdowqieurodisjf"),
        Option("{\"id\":\"/infrastructure/decima\",\"container\":{\"docker\":{\"network\":\"BRIDGE\",\"portMappings\":[{\"containerPort\":7474,\"hostPort\":7474,\"protocol\":\"tcp\"}],\"image\":\"registry.docker.aws-us-west-2-infrastructure.socrata.net:5000/internal/decima:0.1.3_a89227c3_640\"},\"type\":\"DOCKER\",\"volumes\":[]},\"env\":{\"CLORTHO_BUCKET\":\"infrastructure-credentials-bucket-credsbucket-xu43gg2w2hck\",\"PG_DB_HOST\":\"decima-infrastructure.cq9ts6dqjgr3.us-west-2.rds.amazonaws.com\",\"http_proxy\":\"http://proxy.aws-us-west-2-infrastructure.socrata.net:3128\",\"https_proxy\":\"http://proxy.aws-us-west-2-infrastructure.socrata.net:3128\",\"no_proxy\":\"localhost,127.0.0.1,localaddress,.localdomain.com,169.254.169.254,jenkins\",\"CLORTHO_PATH\":\"decima-db-pw.conf\",\"PG_DB_PASSWORD_LINE\":\"include \\\"/dev/shm/decima-db-pw.conf\\\"\"},\"constraints\":[[\"hostname\",\"UNIQUE\"],[\"az\",\"GROUP_BY\"],[\"dmz\",\"LIKE\",\"false\"]],\"args\":[],\"cpus\":0.2,\"mem\":512,\"command\":\"\",\"instances\":2,\"upgradeStrategy\":{\"minimumHealthCapacity\":0.5},\"healthChecks\":[{\"path\":\"/version\",\"protocol\":\"HTTP\",\"timeoutSeconds\":20,\"maxConsecutiveFailures\":0}]}"), // scalastyle:ignore line.size.limit
        "an engineer",
        "autoprod")
      val body = write(parse(write(newDeploy)).underscoreKeys)
      put("/deploy", body) {
        status should be (200)
        val deploy = parse(response.body).camelizeKeys.extract[Deploy]
        db.withSession { implicit session: Session =>
          dao.lookup(deploy.id) should be ('right)
        }
      }
    }

    "return an error when sent an invalid deploy" in {
      val invalidDeploy = """{"service": "blah", "version": "1.1.4", "blah": "error"}"""
      put("/deploy", invalidDeploy) {
        status should be (500)
        val error = parse(response.body).camelizeKeys.extract[ErrorMessage]
        error.error should be (true)
      }


    }
  }

  "The Deploy Service /deploy/history" should {
    "return the history of recent deploys" in {
      get("/deploy/history") {
        val deploys = parseResponse(response.body)
        deploys.length should be (12)
      }
    }
  }

}
