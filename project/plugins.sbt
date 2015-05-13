addSbtPlugin("com.mojolly.scalate" % "xsbt-scalate-generator" % "0.5.0")

addSbtPlugin("org.scalatra.sbt" % "scalatra-sbt" % "0.3.5")

// addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.13.0")

resolvers += "Socrata Cloudbees" at "https://repository-socrata-oss.forge.cloudbees.com/release"

addSbtPlugin("com.socrata" % "socrata-sbt-plugins" % "1.4.3")

addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.4.0")
