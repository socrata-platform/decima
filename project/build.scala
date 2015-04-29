import sbt._
import Keys._
import org.scalatra.sbt._
import org.scalatra.sbt.PluginKeys._
import com.mojolly.scalate.ScalatePlugin._
import ScalateKeys._

object DecimaBuild extends Build {
  val Organization = "com.socrata"
  val Name = "decima"
  val Version = "0.1.0-SNAPSHOT"
  val ScalaVersion = "2.11.6"
  val ScalatraVersion = "2.3.1"
  val LiquibaseVersion = "3.3.2"

  lazy val project = Project (
    "decima",
    file("."),
    settings = ScalatraPlugin.scalatraWithJRebel ++ scalateSettings ++ Seq(
      organization := Organization,
      name := Name,
      version := Version,
      scalaVersion := ScalaVersion,
      resolvers += Classpaths.typesafeReleases,
      resolvers += "Scalaz Bintray Repo" at "http://dl.bintray.com/scalaz/releases",
      resolvers += "Socrata Cloudbees" at "https://repository-socrata-oss.forge.cloudbees.com/release",
      resolvers += Resolver.url("bintray-sbt-plugins", url("https://dl.bintray.com/sbt/sbt-plugin-releases/"))(Resolver.ivyStylePatterns),
      libraryDependencies ++= Seq(
        "org.scalatra" %% "scalatra" % ScalatraVersion,
        "org.scalatra" %% "scalatra-scalate" % ScalatraVersion,
        "org.scalatra" %% "scalatra-json" % ScalatraVersion,
        "org.scalatra" %% "scalatra-specs2" % ScalatraVersion % "test",
        "org.scalatra" %% "scalatra-scalatest" % ScalatraVersion % "test",
        "org.scalatest" %% "scalatest" % "2.2.4" % "test",
        "org.json4s"   %% "json4s-jackson" % "3.3.0.RC1",
        "com.typesafe.slick" %% "slick" % "2.1.0",
        "org.slf4j" % "slf4j-api" % "1.7.10",
        "c3p0" % "c3p0" % "0.9.1.2",
        "ch.qos.logback" % "logback-classic" % "1.1.2" % "runtime",
        "org.eclipse.jetty" % "jetty-webapp" % "9.1.5.v20140505" % "container;compile",
        "org.eclipse.jetty" % "jetty-plus" % "9.1.5.v20140505" % "container",
        "javax.servlet" % "javax.servlet-api" % "3.1.0",
        "org.liquibase" % "liquibase-core" % LiquibaseVersion,
        "org.liquibase" % "liquibase-maven-plugin" % LiquibaseVersion,
        "com.typesafe" % "config" % "1.2.1",
        "org.postgresql" % "postgresql" % "9.4-1201-jdbc4"
      ),
      scalateTemplateConfig in Compile <<= (sourceDirectory in Compile){ base =>
        Seq(
          TemplateConfig(
            base / "webapp" / "WEB-INF" / "templates",
            Seq.empty,  /* default imports should be added here */
            Seq(
              Binding("context", "_root_.org.scalatra.scalate.ScalatraRenderContext", importMembers = true, isImplicit = true)
            ),  /* add extra bindings here */
            Some("templates")
          )
        )
      }
    )
  )
}
