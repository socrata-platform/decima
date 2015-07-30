package com.socrata.decima.models

case class AutoprodInfo(service: String,
                        project: String,
                        environment: String,
                        buildId: Option[String],
                        deployedBy: String,
                        deployMethod: String)
