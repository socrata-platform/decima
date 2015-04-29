package com.socrata

import com.socrata.decima.DecimaConfig
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.DefaultServlet
import org.eclipse.jetty.webapp.WebAppContext
import org.scalatra.servlet.ScalatraListener

/**
 * JettyLauncher class to enable launching the service from a standalone JAR
 * specified in the SBT build configuration
 */
object JettyLauncher {
  def main(args: Array[String]) {
    val server = new Server(DecimaConfig.App.port)
    val context = new WebAppContext()

    context setContextPath "/"
    context.setResourceBase("src/main/webapp")
    context.addEventListener(new ScalatraListener)
    context.addServlet(classOf[DefaultServlet], "/")

    server.setHandler(context)
    server.start
    server.join
  }
}
