package com.socrata.decima.models

import org.joda.time.DateTime

case class Deploy(id: Long,
                   service: String,
                   environment: String,
                   version: String,
                   serviceSha: String,
                   dockerSha: Option[String],
                   configuration: Option[String],
                   deployedBy: String,
                   deployMethod: String,
                   deployedAt: DateTime)

case class DeployForCreate(service: String,
                            environment: String,
                            version: String,
                            serviceSha: String,
                            dockerSha: Option[String],
                            configuration: Option[String],
                            deployedBy: String,
                            deployMethod: String)
