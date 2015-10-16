package com.socrata.decima.models

import com.socrata.decima.util.TimeUtils
import org.joda.time.DateTime

case class Verification(
                        status: String,
                        deployId: Long,
                        id: Long = 0,
                        details: Option[String] = None,
                        time: DateTime = TimeUtils.now
                       )