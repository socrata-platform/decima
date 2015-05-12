package com.socrata.decima

import com.socrata.decima.services.VersionService
import org.scalatest._
import org.scalatra.test.scalatest.ScalatraSuite

// scalastyle:off multiple.string.literals
// scalastyle:off magic.number

class VersionServiceSpec extends ScalatraSuite with WordSpecLike with BeforeAndAfter
                          with ShouldMatchers {

  addServlet(classOf[VersionService], "/version")

  "The Version Service" should {
    "return the version on a GET request" in {
      get("version") {
        status should be (200)
        assert(response.body.contains("version"))
      }
    }
  }

}
