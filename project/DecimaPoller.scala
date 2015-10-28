import sbt._
import Keys._
import sbtbuildinfo.BuildInfoPlugin
import Dependencies._
import scoverage.ScoverageSbtPlugin

object DecimaPoller {
  lazy val settings: Seq[Setting[_]] =
    BuildSettings.projectSettings(assembly = true) ++
      Seq(
        mainClass := Some("com.socrata.decima.Poller"),
        libraryDependencies ++=
          Groups.Akka ++
          Seq(
            awsSqs
          ),
        ScoverageSbtPlugin.ScoverageKeys.coverageMinimum := 50 // for now, decrease required minimum code coverage
      )

  lazy val plugins: Seq[Plugins.Basic] = Seq(
    BuildInfoPlugin
  )
}
