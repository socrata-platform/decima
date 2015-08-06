resolvers += "Socrata Cloudbees" at "https://repository-socrata-oss.forge.cloudbees.com/release"

addSbtPlugin("com.mojolly.scalate" % "xsbt-scalate-generator" % "0.5.0")
addSbtPlugin("org.scalatra.sbt" % "scalatra-sbt" % "0.3.5")
addSbtPlugin("com.socrata" % "socrata-sbt-plugins" % "1.6.0")
addSbtPlugin("org.scoverage" % "sbt-coveralls" % "1.0.0")