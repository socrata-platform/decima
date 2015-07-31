package com.socrata.decima.services

import org.json4s.Formats
import org.scalatra.ScalatraServlet
import org.scalatra.json.JacksonJsonSupport

/**
 * DecimaServlet serves the landing page for Decima
 */
class DecimaServlet extends ScalatraServlet with JacksonJsonSupport {

  override protected implicit def jsonFormats: Formats = org.json4s.DefaultFormats

  get("/") {
    <html>
      <body>
        <h1>Hello, world!</h1>
        This is Decima, a service for keeping track of deploys. Check out the following endpoints:
        <ul>
          <li><b>/deploy GET:</b> information about the currently deployed services</li>
          <li><b>/deploy PUT:</b> notify Decima about a deploy event</li>
          <li><b>/deploy/history GET:</b> get history of recent deploys</li>
        </ul>
      </body>
    </html>
  }

  get("/version") {
    contentType = formats("json")
    buildinfo.BuildInfo.toJson
  }
}
