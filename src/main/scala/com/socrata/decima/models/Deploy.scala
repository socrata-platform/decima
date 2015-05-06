package com.socrata.decima.models

import org.joda.time.DateTime

case class Deploy(id: Int,
                   service: String,
                   environment: String,
                   version: String,
                   git: Option[String],
                   deployedBy: String,
                   deployedAt: DateTime)

case class DeployForCreate(service: String,
                            environment: String,
                            version: String,
                            git: Option[String],
                            deployedBy: String)
