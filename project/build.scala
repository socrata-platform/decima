import sbt._
import Keys._
import org.scalatra.sbt._
import com.mojolly.scalate.ScalatePlugin._
import sbtassembly.{MergeStrategy, AssemblyKeys}
import sbtassembly.AssemblyKeys._
import sbtbuildinfo.BuildInfoPlugin
import scoverage.ScoverageSbtPlugin

object DecimaBuild extends Build {
  private val ScalatraVersion = "2.3.1"
  private val JettyVersion = "9.1.5.v20140505"
  private val Json4sVersion = "3.3.0.RC1"

  lazy val project = Project (
    "decima",
    file("."),
    settings = ScalatraPlugin.scalatraWithJRebel ++ scalateSettings ++ Seq(
      organization := "com.socrata",
      name := "decima",
      scalaVersion := "2.11.6",
      crossScalaVersions := Seq("2.11.6"),
      mainClass in (Compile, run) := Some("com.socrata.decima.JettyLauncher"),
      mainClass in assembly := Some("com.socrata.decima.JettyLauncher"),
      resolvers += Classpaths.typesafeReleases,
      resolvers += "Scalaz Bintray Repo" at "http://dl.bintray.com/scalaz/releases",
      resolvers += "Socrata Cloudbees" at "https://repository-socrata-oss.forge.cloudbees.com/release",
      resolvers += Resolver.url("bintray-sbt-plugins", url("https://dl.bintray.com/sbt/sbt-plugin-releases/"))(Resolver.ivyStylePatterns),
      libraryDependencies ++= Seq(
        "com.github.seratch"  %% "awscala"              % "0.5.+",
        "com.typesafe.slick"  %% "slick"                % "2.1.0",
        "org.clapper"         %% "grizzled-slf4j"       % "1.0.2",
        "org.json4s"          %% "json4s-ext"           % Json4sVersion,
        "org.json4s"          %% "json4s-jackson"       % Json4sVersion,
        "org.scalatra"        %% "scalatra"             % ScalatraVersion,
        "org.scalatra"        %% "scalatra-json"        % ScalatraVersion,
        "org.scalatra"        %% "scalatra-scalate"     % ScalatraVersion,
        "c3p0"                % "c3p0"                  % "0.9.1.2",
        "ch.qos.logback"      % "logback-classic"       % "1.1.2"             % "runtime",
        "com.typesafe"        % "config"                % "1.2.1",
        "javax.servlet"       % "javax.servlet-api"     % "3.1.0",
        "org.eclipse.jetty"   % "jetty-plus"            % JettyVersion        % "container",
        "org.eclipse.jetty"   % "jetty-webapp"          % JettyVersion        % "container;compile",
        "org.liquibase"       % "liquibase-core"        % "3.3.3",
        "org.postgresql"      % "postgresql"            % "9.4-1201-jdbc4",
        "org.slf4j"           % "slf4j-api"             % "1.7.10",
        "org.yaml"            % "snakeyaml"             % "1.15",
        "org.scalatest"       %% "scalatest"            % "2.2.4"             % "test",
        "org.scalatra"        %% "scalatra-scalatest"   % ScalatraVersion     % "test",
        "com.h2database"      % "h2"                    % "1.4.180"           % "test"
      ),
      assemblyMergeStrategy in assembly := {
        case "mime.types" => MergeStrategy.first
        case x =>
          val oldStrategy = (assemblyMergeStrategy in assembly).value
          oldStrategy(x)
      },
      ScoverageSbtPlugin.ScoverageKeys.coverageExcludedPackages := ".*ScalatraBootstrap;.*JettyLauncher;.*MigrateSchema;.*templates.*;.*Migration;"
    )
  ).enablePlugins(BuildInfoPlugin)

  lazy val gitSha = Process(Seq("git", "describe", "--always", "--dirty", "--long", "--abbrev=10")).!!.stripLineEnd
}
