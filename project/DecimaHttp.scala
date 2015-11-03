import sbt._
import Keys._
import org.scalatra.sbt._
import com.mojolly.scalate.ScalatePlugin._
import sbtbuildinfo.BuildInfoPlugin
import Dependencies._

object DecimaHttp {
  lazy val settings: Seq[Setting[_]] =
    BuildSettings.webProjectSettings(assembly = true) ++
    ScalatraPlugin.scalatraWithJRebel ++
    scalateSettings ++
      Seq(
        mainClass := Some("com.socrata.decima.JettyLauncher"),
        libraryDependencies ++= Groups.Scalatra
      )

  lazy val plugins: Seq[Plugins.Basic] = Seq(
    BuildInfoPlugin
  )
}
