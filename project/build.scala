import sbt._
import Keys._
import org.scalatra.sbt._
import com.mojolly.scalate.ScalatePlugin._
import sbtassembly.AssemblyKeys
import AssemblyKeys._

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
      mainClass in (Compile, run) := Some("com.socrata.JettyLauncher"),
      mainClass in assembly := Some("com.socrata.JettyLauncher"),
      resolvers += Classpaths.typesafeReleases,
      resolvers += "Scalaz Bintray Repo" at "http://dl.bintray.com/scalaz/releases",
      resolvers += "Socrata Cloudbees" at "https://repository-socrata-oss.forge.cloudbees.com/release",
      resolvers += Resolver.url("bintray-sbt-plugins", url("https://dl.bintray.com/sbt/sbt-plugin-releases/"))(Resolver.ivyStylePatterns),
      libraryDependencies ++= Seq(
        "org.scalatra"        %% "scalatra"             % ScalatraVersion,
        "org.scalatra"        %% "scalatra-scalate"     % ScalatraVersion,
        "org.scalatra"        %% "scalatra-json"        % ScalatraVersion,
        "org.scalatra"        %% "scalatra-scalatest"   % ScalatraVersion     % "test",
        "org.scalatest"       %% "scalatest"            % "2.2.4"             % "test",
        "org.json4s"          %% "json4s-jackson"       % Json4sVersion,
        "org.json4s"          %% "json4s-ext"           % Json4sVersion,
        "com.typesafe.slick"  %% "slick"                % "2.1.0",
        "org.slf4j"           % "slf4j-api"             % "1.7.10",
        "c3p0"                % "c3p0"                  % "0.9.1.2",
        "ch.qos.logback"      % "logback-classic"       % "1.1.2"             % "runtime",
        "org.eclipse.jetty"   % "jetty-webapp"          % JettyVersion        % "container;compile",
        "org.eclipse.jetty"   % "jetty-plus"            % JettyVersion        % "container",
        "javax.servlet"       % "javax.servlet-api"     % "3.1.0",
        "com.typesafe"        % "config"                % "1.2.1",
        "org.postgresql"      % "postgresql"            % "9.4-1201-jdbc4",
        "com.h2database"      % "h2"                    % "1.4.180"           % "test"
      )
//      sourceGenerators in Compile <+= buildInfo,
//      buildInfoPackage := "com.socrata.decima",
//      buildInfoKeys := Seq[BuildInfoKey](
//        name,
//        version,
//        scalaVersion,
//        libraryDependencies in Compile,
//        BuildInfoKey.action("buildTime") { System.currentTimeMillis }
//      )
    )
  )
}
