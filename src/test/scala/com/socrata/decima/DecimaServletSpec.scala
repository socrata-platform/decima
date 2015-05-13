package com.socrata.decima

import com.socrata.decima.services.DecimaServlet
import org.json4s._
import org.scalatest.FunSuiteLike
import org.scalatra.test.scalatest._

// scalastyle:off magic.number
// scalastyle:off multiple.string.literals

class DecimaServletSpec extends ScalatraSuite with FunSuiteLike {

  implicit val formats = DefaultFormats ++ org.json4s.ext.JodaTimeSerializers.all
  addServlet(classOf[DecimaServlet], "/*")

  test("simple get should return 200") {
    get("/") {
      status should equal(200)
    }
  }

  test("return the version on a GET request") {
    get("version") {
      status should be(200)
      assert(response.body.contains("version"))
    }
  }
}
