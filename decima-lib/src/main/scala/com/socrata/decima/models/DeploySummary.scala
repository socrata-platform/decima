package com.socrata.decima.models

case class DeploySummary(serviceAlias: String,
                         parity: Boolean,
                         referenceVersion: Option[String],
                         referenceDockerTag: Option[String],
                         referenceServiceSha: Option[String],
                         referenceEnvironment: String,
                         environments: Map[String, Seq[EnvironmentDeploySummary]])
