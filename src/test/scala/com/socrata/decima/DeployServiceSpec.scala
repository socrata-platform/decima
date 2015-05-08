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
        deploys.length should be (5)
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
                                      Option("asdfoqiweurqweorasdflkajsdowqieurodisjf"),
                                      "an engineer",
                                      "autoprod")
      val body = write(parse(write(newDeploy)).underscoreKeys)
      put("/deploy", body) {
        status should be (200)
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
        deploys.length should be (11)
      }
    }
  }

}
