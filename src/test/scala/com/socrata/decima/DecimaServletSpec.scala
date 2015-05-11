package com.socrata.decima

import com.socrata.decima.services.DecimaServlet
import org.scalatra.test.scalatest._
import org.scalatest.FunSuiteLike
import org.json4s._
import org.json4s.jackson.JsonMethods._

// scalastyle:off magic.number

class DecimaServletSpec extends ScalatraSuite with FunSuiteLike {

  implicit val formats = DefaultFormats ++ org.json4s.ext.JodaTimeSerializers.all
  addServlet(classOf[DecimaServlet], "/*")

  test("simple get should return 200") {
    get("/") {
      status should equal (200)
    }
  }

  test("get request to version endpoint should return version info") {
    get("version") {
      status should equal (200)
      assert(response.body.contains("version"))
    }
  }
}
