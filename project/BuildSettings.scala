import sbt._
import Keys._
import sbtassembly.{MergeStrategy, AssemblyKeys}
import sbtassembly.AssemblyKeys._
import scoverage.ScoverageSbtPlugin

object BuildSettings {
  def buildSettings: Seq[Setting[_]] =
    Defaults.itSettings ++
      Seq(
        organization := "com.socrata",
        scalaVersion := "2.11.6",
        crossScalaVersions := Seq("2.11.6"),
        resolvers ++= Seq(
          Classpaths.typesafeReleases,
          "Scalaz Bintray Repo" at "http://dl.bintray.com/scalaz/releases",
          "Socrata Cloudbees" at "https://repository-socrata-oss.forge.cloudbees.com/release",
          Resolver.url("bintray-sbt-plugins", url("https://dl.bintray.com/sbt/sbt-plugin-releases/"))(Resolver.ivyStylePatterns)
        ),
        assemblyMergeStrategy in assembly := {
          case "mime.types" => MergeStrategy.first
          case x =>
            val oldStrategy = (assemblyMergeStrategy in assembly).value
            oldStrategy(x)
        },
        ScoverageSbtPlugin.ScoverageKeys.coverageExcludedPackages := ".*ScalatraBootstrap;.*JettyLauncher;.*MigrateSchema;.*templates.*;.*Migration;"
      )

  def projectSettings(assembly: Boolean = false): Seq[Setting[_]] = {
    buildSettings ++ (if (!assembly) Seq(AssemblyKeys.assembly := file(".")) else Nil)
  }
}