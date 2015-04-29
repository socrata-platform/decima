package com.socrata.decima

import org.scalatra.test.scalatest._
import org.scalatest.FunSuiteLike

class DecimaServletSpec extends ScalatraSuite with FunSuiteLike {

  addServlet(classOf[DecimaServlet], "/*")

  test("simple get should return 200") {
    get("/") {
      status should equal (200)
    }
  }
}
