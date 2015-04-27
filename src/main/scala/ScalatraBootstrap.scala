import com.socrata.decima.lachesis._
import com.typesafe.config.ConfigFactory
import org.scalatra._
import javax.servlet.ServletContext

import com.mchange.v2.c3p0.ComboPooledDataSource
import scala.slick.jdbc.JdbcBackend.Database
import org.slf4j.LoggerFactory

class ScalatraBootstrap extends LifeCycle {

  val logger = LoggerFactory.getLogger(getClass)

  println(Class.forName("org.postgresql.Driver"))
  val cpds = new ComboPooledDataSource("c3p0")
  logger.info("Created c3p0 connection pool")
  logger.info("JDBC URL: " + cpds.getJdbcUrl)

  override def init(context: ServletContext) {
    val db = Database.forDataSource(cpds)
    context.mount(new DeployController(db), "/deploy/*")
    context.mount(new LachesisServlet, "/*")
  }

  private def closeDbConnection() {
    logger.info("Closing c3p0 connection pool")
    cpds.close
  }

  override def destroy(context: ServletContext) {
    super.destroy(context)
    closeDbConnection
  }
}
