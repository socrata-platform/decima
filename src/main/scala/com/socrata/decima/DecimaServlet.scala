package com.socrata.decima

/**
 * DecimaServlet serves the landing page for Decima
 */
class DecimaServlet extends DecimaStack {

  get("/") {
    before() {
      contentType = formats("html")
    }
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

//  get("/version") {
//    Map("version" -> BuildInfo.version,
//      "scalaVersion" -> BuildInfo.scalaVersion,
//      "dependencies" -> BuildInfo.libraryDependencies,
//      "buildTime" -> new org.joda.time.DateTime(BuildInfo.buildTime).toString())
//    )
//  }

}
