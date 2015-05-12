import javax.servlet.ServletContext

import com.mchange.v2.c3p0.ComboPooledDataSource
import com.socrata.decima.data_access.DeployAccessWithPostgres
import com.socrata.decima.database.{ActualPostgresDriver, DeployDAO}
import com.socrata.decima.services.{VersionService, DecimaServlet, DeployService}
import com.socrata.decima.util.DecimaConfig
import org.scalatra._
import org.slf4j.LoggerFactory

import scala.slick.jdbc.JdbcBackend._


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
   * @param context context for servlet
   */
  override def init(context: ServletContext): Unit = {
    val db = Database.forDataSource(cpds)
    val deployAccess = new DeployAccessWithPostgres(db, new DeployDAO() with ActualPostgresDriver)
    context.mount(new DeployService(deployAccess), "/deploy/*")
    context.mount(new VersionService, "/version")
    context.mount(new DecimaServlet, "/*")
  }

  /**
   * Clean up database connection, called in destroy func
   */
  private def closeDbConnection(): Unit = {
    logger.info("Closing c3p0 connection pool")
    cpds.close()
  }

  /**
   * Destroys app on shutdown of service
   * @param context servlet context
   */
  override def destroy(context: ServletContext): Unit = {
    super.destroy(context)
    closeDbConnection()
  }
}
