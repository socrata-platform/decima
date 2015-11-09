package com.socrata.decima

import com.socrata.decima.http.DecimaServlet
import org.json4s._
import org.scalatest.FunSuiteLike
import org.scalatra.test.scalatest._
import org.eclipse.jetty.servlet.ServletContextHandler
import org.scalatra.servlet.ScalatraListener

class DecimaServletSpec extends ScalatraSuite with FunSuiteLike {

  implicit val formats = DefaultFormats ++ org.json4s.ext.JodaTimeSerializers.all
  addServlet(classOf[DecimaServlet], "/*")

  // When running the servlet in tests the context path is set to '/'
  // resulting in the resouce search path evaluating to <project root>/src/main/webapp
  // instead of what it should be <project root>/decima-http/src/main/webapp
  // this is a workaround for this in testing
  // TODO :: Figure out why this is only happens in testing.
  override lazy val servletContextHandler = {
    val handler = new ServletContextHandler(ServletContextHandler.SESSIONS)
    handler.setContextPath(contextPath)
    handler.addEventListener(new ScalatraListener)
    handler.setResourceBase("decima-http/src/main/webapp")
    handler
  }

  test("simple get should return 200") {
    get("/") {
      status should equal(200)
    }
  }

  test("simple get on service should return 200") {
    get("/service/core") {
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
