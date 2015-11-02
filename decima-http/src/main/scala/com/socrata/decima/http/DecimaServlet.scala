package com.socrata.decima.http

import com.socrata.decima.util.JsonFormats
import org.json4s.Formats
import org.scalatra.ScalatraServlet
import org.scalatra.json.JacksonJsonSupport

/**
 * DecimaServlet serves the landing page for Decima
 */
class DecimaServlet extends ScalatraServlet with JacksonJsonSupport {

  override protected implicit def jsonFormats: Formats = JsonFormats.Formats

  get("/") {
    """
    <html>
      <head>
        <script src="https://ajax.googleapis.com/ajax/libs/jquery/2.1.4/jquery.js"></script>
        <script src="https://cdnjs.cloudflare.com/ajax/libs/underscore.js/1.8.3/underscore.js"></script>
        <script src="https://cdnjs.cloudflare.com/ajax/libs/handlebars.js/3.0.3/handlebars.js"></script>
        <script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/js/bootstrap.min.js"></script>
        <script src="https://cdnjs.cloudflare.com/ajax/libs/moment.js/2.10.6/moment.js"></script>

        <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/css/bootstrap.min.css">
        <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/css/bootstrap-theme.min.css">

        <script src="/script/app.js"></script>
        <link rel="stylesheet" href="/style/app.css">
      </head>
      <body>
        <div class="container">
          <div class="header">
            <a class="clearfix header-link" href="#">
              <img class="header-img" width="201" height="100" src="/images/socrata-logo.svg">
            </a>
            <div class="header-text">Decima -- Deployment Tracking</div>
          </div>
            <div class="container" id="services-table-rows"></div>
          </div>
        </div>
        <script id="service-template" type="text/x-handlebars-template">
          <div class="service row">
            <!--<h3>{{service_alias}}</h3>-->
            <div class="col-md-3 service-name">{{service_alias}}</div>
            {{#each env_parity}}
            <div class="col-md-2 {{col_class}} {{match_class}}">
              {{environment}}
            </div>
            {{/each}}
          </div>
        </script>
      </body>
    </html>
    """
  }

  get("/version") {
    contentType = formats("json")
    buildinfo.BuildInfo.toJson
  }
}
