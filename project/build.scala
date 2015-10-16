import sbt._

object DecimaBuild extends sbt.Build {
  lazy val build = Project(
    "decima",
    file(".")
  ).settings(BuildSettings.buildSettings : _*)
   .aggregate(decimaLib, decimaService)

  lazy val gitSha = Process(Seq("git", "describe", "--always", "--dirty", "--long", "--abbrev=10")).!!.stripLineEnd

  // Helper for separating modules and defining module dependencies
  private def p(name: String, settings: { def settings: Seq[Setting[_]]; def plugins: Seq[Plugins.Basic] }, dependencies: ProjectReference*) = {
    Project(name, file(name))
      .settings(settings.settings: _*)
      .configs(IntegrationTest)
      .dependsOn(dependencies.map(dep => dep % "compile->compile;test->test"): _*)
      .enablePlugins(settings.plugins: _*)
  }

  val decimaLib = p("decima-lib", DecimaLib)
  val decimaService = p("decima-http", DecimaHttp, decimaLib)
}
