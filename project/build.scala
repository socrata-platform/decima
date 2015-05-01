import sbt._
import Keys._
import org.scalatra.sbt._
import com.mojolly.scalate.ScalatePlugin._

object DecimaBuild extends Build {
  private val ScalatraVersion = "2.3.1"

  lazy val project = Project (
    "decima",
    file("."),
    settings = ScalatraPlugin.scalatraWithJRebel ++ scalateSettings ++ Seq(
      organization := "com.socrata",
      name := "decima",
      scalaVersion := "2.11.6",
      resolvers += Classpaths.typesafeReleases,
      resolvers += "Scalaz Bintray Repo" at "http://dl.bintray.com/scalaz/releases",
      resolvers += "Socrata Cloudbees" at "https://repository-socrata-oss.forge.cloudbees.com/release",
      resolvers += Resolver.url("bintray-sbt-plugins", url("https://dl.bintray.com/sbt/sbt-plugin-releases/"))(Resolver.ivyStylePatterns),
      libraryDependencies ++= Seq(
        "org.scalatra"        %% "scalatra"             % ScalatraVersion,
        "org.scalatra"        %% "scalatra-scalate"     % ScalatraVersion,
        "org.scalatra"        %% "scalatra-json"        % ScalatraVersion,
        "org.scalatra"        %% "scalatra-specs2"      % ScalatraVersion % "test",
        "org.scalatra"        %% "scalatra-scalatest"   % ScalatraVersion % "test",
        "org.scalatest"       %% "scalatest"            % "2.2.4" % "test",
        "org.json4s"          %% "json4s-jackson"       % "3.3.0.RC1",
        "com.typesafe"        %% "slick"                % "3.0.0",
        "org.slf4j"           % "slf4j-api"             % "1.7.10",
        "c3p0"                % "c3p0"                  % "0.9.1.2",
        "ch.qos.logback"      % "logback-classic"       % "1.1.2" % "runtime",
        "org.eclipse.jetty"   % "jetty-webapp"          % "9.1.5.v20140505" % "container;compile",
        "org.eclipse.jetty"   % "jetty-plus"            % "9.1.5.v20140505" % "container",
        "javax.servlet"       % "javax.servlet-api"     % "3.1.0",
        "com.typesafe"        % "config"                % "1.2.1",
        "org.postgresql"      % "postgresql"            % "9.4-1201-jdbc4"
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
