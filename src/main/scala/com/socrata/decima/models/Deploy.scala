package com.socrata.decima.models

import org.joda.time.DateTime

case class Deploy(id: Long,
                   service: String,
                   environment: String,
                   version: String,
                   dockerTag: Option[String],
                   serviceSha: String,
                   dockerSha: Option[String],
                   configuration: Option[String],
                   deployedBy: String,
                   deployMethod: String,
                   deployedAt: DateTime,
                   verified: String)

case class DeployForCreate(service: String,
                            environment: String,
                            version: String,
                            dockerTag: Option[String],
                            serviceSha: String,
                            dockerSha: Option[String],
                            configuration: Option[String],
                            deployedBy: String,
                            deployMethod: String)
