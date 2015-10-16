package com.socrata.decima.models

import com.socrata.decima.util.TimeUtils
import org.joda.time.DateTime

case class Deploy(
                   service: String,
                   environment: String,
                   version: String,
                   dockerTag: Option[String],
                   serviceSha: String,
                   dockerSha: Option[String],
                   configuration: Option[String],
                   deployedBy: String,
                   deployMethod: String,
                   id: Long = 0,
                   deployedAt: DateTime = TimeUtils.now,
                   verification: Option[Verification] = None
                 )