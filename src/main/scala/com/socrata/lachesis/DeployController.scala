package com.socrata.lachesis

// JSON-related libraries
import org.json4s.{DefaultFormats, Formats}

// JSON handling support from Scalatra
import org.scalatra.json._

import org.scalatra.ScalatraServlet

/**
 * Created by michaelbrown on 4/22/15.
 */
class DeployController extends ScalatraServlet with JacksonJsonSupport {
  // Sets up automatic case class to JSON output serialization, required by
  // the JValueResult trait.
  protected implicit val jsonFormats: Formats = DefaultFormats

  // Before every action runs, set the content type to be in JSON format.
  before() {
    contentType = formats("json")
  }

  get("/") {
    var deploys = DeployData.all

    val serviceName = params.getOrElse("service", null)
    val environmentName = params.getOrElse("environment", null)


    if (serviceName != null) {
      deploys = deploys.filter((d: Deployment) => d.service == serviceName)
    }

    if (environmentName != null) {
      deploys = deploys.filter((d: Deployment) => d.environment == environmentName)
    }

    deploys
  }
}

case class Deployment(service: String, version: String, environment: String)

object DeployData {

  /**
   * Fake data about deploys
   */
  var all = List(
    Deployment("phidippides", "0.1.0", "production"),
    Deployment("phidippides", "0.1.0", "staging"),
    Deployment("phidippides", "0.1.1", "staging"),
    Deployment("frontend", "0.2.0", "production"),
    Deployment("frontend", "0.2.1", "production"),
    Deployment("procrustes", "0.1.4", "production"),
    Deployment("procrustes", "0.1.5", "staging"),
    Deployment("procrustes", "0.1.6", "staging"),
    Deployment("core", "0.2.3", "production"),
    Deployment("core", "0.2.7", "production"),
    Deployment("phidippides", "2.1.0", "production"),
    Deployment("phidippides", "3.1.0", "staging"),
    Deployment("phidippides", "0.4.5", "staging"),
    Deployment("frontend", "0.2.6", "production"),
    Deployment("frontend", "0.2.41", "production"),
    Deployment("phidippides", "0.31.0", "production"),
    Deployment("phidippides", "0.14.0", "staging"),
    Deployment("phidippides", "0.15.6", "staging"),
    Deployment("frontend", "0.5.3", "production"),
    Deployment("frontend", "0.6.1", "production"),
    Deployment("phidippides", "0.6.0", "production"),
    Deployment("phidippides", "0.1.7", "staging"),
    Deployment("phidippides", "0.1.1", "staging"),
    Deployment("frontend", "0.2.6", "production"),
    Deployment("frontend", "0.2.7", "production"),
    Deployment("phidippides", "0.1.7", "production"),
    Deployment("phidippides", "0.1.3", "staging"),
    Deployment("phidippides", "0.1.6", "staging"),
    Deployment("frontend", "0.2.3", "production"),
    Deployment("frontend", "0.2.2", "production")
  )
}