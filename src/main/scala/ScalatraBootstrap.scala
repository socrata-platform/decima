import javax.servlet.ServletContext

import com.mchange.v2.c3p0.ComboPooledDataSource
import com.socrata.decima._
import org.scalatra._
import org.slf4j.LoggerFactory

import scala.slick.jdbc.JdbcBackend.Database

/**
 * ScalatraBootstrap class for global app settings and lifecycle management
 */
class ScalatraBootstrap extends LifeCycle {

  val logger = LoggerFactory.getLogger(getClass)

  // Set up DB configuration
  // TODO: This config stuff should be automatic
  val cpds = new ComboPooledDataSource
  cpds.setJdbcUrl(DecimaConfig.Db.jdbcUrl)
  cpds.setUser(DecimaConfig.Db.user)
  cpds.setPassword(DecimaConfig.Db.password)
  logger.info("Created c3p0 connection pool")
  logger.info("JDBC URL: " + cpds.getJdbcUrl)

  /**
   * Initialize app and set routing configuration
   * @param context
   */
  override def init(context: ServletContext) {
    val db = Database.forDataSource(cpds)
    context.mount(new DeployController(db), "/deploy/*")
    context.mount(new DecimaServlet, "/*")
  }

  /**
   * Clean up database connection, called in destroy func
   */
  private def closeDbConnection() {
    logger.info("Closing c3p0 connection pool")
    cpds.close
  }

  /**
   * Destroys app on shutdown of service
   * @param context
   */
  override def destroy(context: ServletContext) {
    super.destroy(context)
    closeDbConnection
  }
}
