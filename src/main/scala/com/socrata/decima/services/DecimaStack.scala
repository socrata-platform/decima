package com.socrata.decima.services

import org.json4s._
import org.scalatra._
import org.scalatra.json.JacksonJsonSupport
import org.slf4j.LoggerFactory

trait DecimaStack extends ScalatraServlet with JacksonJsonSupport with ScalatraLogging {

  // Sets up automatic case class to JSON output serialization, required by
  // the JValueResult trait.
  protected implicit val jsonFormats: Formats = org.json4s.DefaultFormats ++ org.json4s.ext.JodaTimeSerializers.all
  override protected def transformRequestBody(body: JValue): JValue = body.camelizeKeys
  override protected def transformResponseBody(body: JValue): JValue = body.underscoreKeys

  // Before every action runs, set the content type to be in JSON format.
  before() {
    contentType = formats("json")
  }

  error {
    case e: Exception =>
      logger.error("Request error: ", e)
      InternalServerError(ErrorMessage(error = true, e.getClass.getSimpleName, e.getMessage, e.getStackTrace.map(s => s.toString)))
  }

}

// TODO: Move this to thirdparty-utils
trait ScalatraLogging extends ScalatraServlet {
  val logger = LoggerFactory.getLogger(getClass)

  before() {
    logger.info(request.getMethod + " - " + request.getRequestURI + " ? " + request.getQueryString)
  }

  after() {
    logger.info("Status - " + response.getStatus)
  }
}

case class ErrorMessage(error: Boolean, exception: String, message: String, stackTrace: Array[String])