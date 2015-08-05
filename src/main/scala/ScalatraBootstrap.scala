import javax.servlet.ServletContext

import com.socrata.decima.data_access._
import com.socrata.decima.database._
import com.socrata.decima.services._
import com.socrata.decima.util._
import grizzled.slf4j.Logging
import org.scalatra._

import scala.slick.jdbc.JdbcBackend._


/**
 * ScalatraBootstrap class for global app settings and lifecycle management
 */
class ScalatraBootstrap extends LifeCycle with Logging {

  val cpds = DataSourceFromConfig(DecimaConfig.db)
  val s3 = AmazonS3ClientFromConfig(DecimaConfig.s3)

  /**
   * Initialize app and set routing configuration
   * @param context context for servlet
   */
  override def init(context: ServletContext): Unit = {
    val db = Database.forDataSource(cpds)
    val deployAccess = new DeploymentAccessWithPostgres(db, new DeploymentDAO() with ActualPostgresDriver)
    val s3Access = new S3Access(s3, DecimaConfig.s3.bucketName)
    context.mount(new DeploymentService(deployAccess, s3Access), "/deploy/*")
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
