package com.socrata.decima

import com.socrata.decima.util.DecimaConfig
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.DefaultServlet
import org.eclipse.jetty.webapp.WebAppContext
import org.scalatra.servlet.ScalatraListener

/**
 * JettyLauncher class to enable launching the service from a standalone JAR
 * specified in the SBT build configuration
 */
object JettyLauncher extends App {
  private val rootPath = "/"

  val server = new Server(DecimaConfig.App.port)
  val context = new WebAppContext()

  context setContextPath rootPath
  context.setResourceBase("src/main/webapp")
  context.addEventListener(new ScalatraListener)
  context.addServlet(classOf[DefaultServlet], rootPath)

  server.setHandler(context)
  server.start
  server.join
}
