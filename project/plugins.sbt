resolvers += Resolver.url("bintray-sbt-plugins", url("https://repo.socrata.com/artifactory/ivy-libs-release-local/"))(Resolver.ivyStylePatterns)

addSbtPlugin("com.mojolly.scalate" % "xsbt-scalate-generator" % "0.5.0")
addSbtPlugin("org.scalatra.sbt" % "scalatra-sbt" % "0.3.5")
addSbtPlugin("com.socrata" % "socrata-sbt-plugins" % "1.6.8")
