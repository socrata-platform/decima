import sbt._
import Keys._
import sbtbuildinfo.BuildInfoPlugin
import Dependencies._

object DecimaPoller {
  lazy val settings: Seq[Setting[_]] =
    BuildSettings.projectSettings(assembly = true) ++
      Seq(
        mainClass := Some("com.socrata.decima.poll.Poller"),
        libraryDependencies ++=
          Groups.Akka ++
          Seq(
            awsSqs,
            elasticMq % "test"
          )
      )

  lazy val plugins: Seq[Plugins.Basic] = Seq(
    BuildInfoPlugin
  )
}
