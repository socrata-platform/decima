import sbt._
import Keys._
import Dependencies._

object DecimaLib {
  lazy val settings: Seq[Setting[_]] =
    BuildSettings.projectSettings() ++
      Seq(
        libraryDependencies ++=
          Groups.Json4s ++
          Groups.Logging ++
          Groups.Postgres ++
            Seq(
              awsS3,
              snakeYaml,
              typesafeConfig,
              scalaTest % "test",
              scalaMock % "test"
            )
      )

  lazy val plugins: Seq[Plugins.Basic] = Seq.empty
}
