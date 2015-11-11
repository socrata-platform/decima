package com.socrata.decima.http

import com.socrata.decima.util.JsonFormats
import org.json4s.Formats
import org.scalatra.ScalatraServlet
import org.scalatra.scalate.ScalateSupport
import org.scalatra.json.JacksonJsonSupport

/**
 * DecimaServlet serves the landing page for Decima
 */
class DecimaServlet extends ScalatraServlet with JacksonJsonSupport with ScalateSupport {

  override protected implicit def jsonFormats: Formats = JsonFormats.Formats

  val serviceParamKey = "service"

  get("/") {
    contentType = "text/html"
    layoutTemplate("/WEB-INF/templates/views/index.ssp")
  }

  get(s"/service/:$serviceParamKey") {
    val service = params.get(serviceParamKey)
    contentType = "text/html"
    layoutTemplate("/WEB-INF/templates/views/service.ssp", "service" -> service.get)
  }

  get("/version") {
    contentType = formats("json")
    buildinfo.BuildInfo.toJson
  }
}
