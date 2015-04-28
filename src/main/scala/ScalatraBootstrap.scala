import com.socrata.decima._
import com.typesafe.config._
import org.scalatra._
import javax.servlet.ServletContext

import com.mchange.v2.c3p0.ComboPooledDataSource
import scala.slick.jdbc.JdbcBackend.Database
import org.slf4j.LoggerFactory

class ScalatraBootstrap extends LifeCycle {

  val logger = LoggerFactory.getLogger(getClass)

  println(Class.forName("org.postgresql.Driver"))
  val cpds = new ComboPooledDataSource
  // TODO: This config stuff should be automatic?
  logger.info("Created c3p0 connection pool")
  cpds.setJdbcUrl(DecimaConfig.db.jdbcUrl)
  cpds.setUser(DecimaConfig.db.user)
  cpds.setPassword(DecimaConfig.db.password)
  logger.info("JDBC URL: " + cpds.getJdbcUrl)

  override def init(context: ServletContext) {
    val db = Database.forDataSource(cpds)
    context.mount(new DeployController(db), "/deploy/*")
    context.mount(new DecimaServlet, "/*")
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
