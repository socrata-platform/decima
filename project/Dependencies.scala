import sbt._

object Dependencies {
  private val ScalatraVersion = "2.3.1"
  private val JettyVersion = "9.1.5.v20140505"
  private val Json4sVersion = "3.3.0.RC1"

  val slick           = "com.typesafe.slick"     %% "slick"                % "2.1.0"
  val grizzledSlf4j   = "org.clapper"            %% "grizzled-slf4j"       % "1.0.2"
  val json4sExt       = "org.json4s"             %% "json4s-ext"           % Json4sVersion
  val json4sJackson   = "org.json4s"             %% "json4s-jackson"       % Json4sVersion
  val scalatra        = "org.scalatra"           %% "scalatra"             % ScalatraVersion
  val scalatraJson    = "org.scalatra"           %% "scalatra-json"        % ScalatraVersion
  val scalatraScalate = "org.scalatra"           %% "scalatra-scalate"     % ScalatraVersion
  val awsS3           = "com.amazonaws"          %  "aws-java-sdk-s3"      % "1.10.+"
  val c3p0            = "c3p0"                   %  "c3p0"                 % "0.9.1.2"
  val logbackClassic  = "ch.qos.logback"         %  "logback-classic"      % "1.1.2"
  val typesafeConfig  = "com.typesafe"           %  "config"               % "1.2.1"
  val javaServletApi  = "javax.servlet"          %  "javax.servlet-api"    % "3.1.0"
  val jettyPlus       = "org.eclipse.jetty"      %  "jetty-plus"           % JettyVersion
  val jettyWebapp     = "org.eclipse.jetty"      %  "jetty-webapp"         % JettyVersion
  val liquibase       = "org.liquibase"          %  "liquibase-core"       % "3.3.3"
  val postgresql      = "org.postgresql"         %  "postgresql"           % "9.4-1201-jdbc4"
  val slf4jApi        = "org.slf4j"              %  "slf4j-api"            % "1.7.10"
  val snakeYaml       = "org.yaml"               %  "snakeyaml"            % "1.15"
  val scalatraTest    = "org.scalatra"           %% "scalatra-scalatest"   % ScalatraVersion
  val h2Db            = "com.h2database"         %  "h2"                   % "1.4.180"
  val scalaTest       = "org.scalatest"          %% "scalatest"            % "2.2.4"
  val scalaMock       = "org.scalamock"          %% "scalamock-scalatest-support" % "3.2"

  object Groups {
    val Json4s = Seq(
      json4sJackson,
      json4sExt
    )
    val Logging = Seq(
      grizzledSlf4j,
      slf4jApi
    )
    val Postgres = Seq(
      postgresql,
      c3p0,
      liquibase,
      slick,
      h2Db % "test"
    )
    val Scalatra = Json4s ++ Seq(
      Dependencies.scalatra,
      scalatraJson,
      scalatraScalate,
      javaServletApi,
      logbackClassic % "runtime",
      jettyPlus      % "container",
      jettyWebapp    % "container;compile",
      scalatraTest   % "test"
    )
  }
}
