package com.socrata.decima

import com.socrata.decima.services.DecimaServlet
import org.scalatra.test.scalatest._
import org.scalatest.FunSuiteLike

// scalastyle:off magic.number

class DecimaServletSpec extends ScalatraSuite with FunSuiteLike {

  addServlet(classOf[DecimaServlet], "/*")

  test("simple get should return 200") {
    get("/") {
      status should equal (200)
    }
  }
}
