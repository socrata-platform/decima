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
          "Socrata Artifactory" at "https://repo.socrata.com/artifactory/libs-release",
          Resolver.url("bintray-sbt-plugins", url("https://dl.bintray.com/sbt/sbt-plugin-releases/"))(Resolver.ivyStylePatterns)
        ),
        assemblyMergeStrategy in assembly := {
          case "mime.types" => MergeStrategy.first
          case x =>
            val oldStrategy = (assemblyMergeStrategy in assembly).value
            oldStrategy(x)
        },
        ScoverageSbtPlugin.ScoverageKeys.coverageExcludedPackages :=
          ".*BuildInfo;.*ScalatraBootstrap;.*JettyLauncher;.*MigrateSchema;.*templates.*;.*Migration;"
      )

  def projectSettings(assembly: Boolean = false): Seq[Setting[_]] = {
    buildSettings ++ (if (!assembly) Seq(AssemblyKeys.assembly := file(".")) else Nil)
  }

  def webProjectSettings(assembly: Boolean = false): Seq[Setting[_]] = {
    // Make sure we bundle static resources for webapps in the jar.
    // Method taken from: http://stackoverflow.com/a/17913254
    projectSettings(assembly = assembly) ++
    Seq(
      // copy web resources to /webapp folder
      resourceGenerators in Compile <+= (resourceManaged, baseDirectory) map {
        (managedBase, base) =>
          val webappBase = base / "src" / "main" / "webapp"
          for {
            (from, to) <- webappBase ** "*" x rebase(webappBase, managedBase / "main" / "webapp")
          } yield {
            Sync.copy(from, to)
            to
          }
      }
    )
  }
}
