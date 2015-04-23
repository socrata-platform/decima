import com.socrata.lachesis._
import org.scalatra._
import javax.servlet.ServletContext

class ScalatraBootstrap extends LifeCycle {
  override def init(context: ServletContext) {
    context.mount(new DeployController, "/deploy/*")
    context.mount(new LachesisServlet, "/*")
  }
}
