package com.socrata.decima.models

import org.joda.time.DateTime

case class Verification (id: Long,
                        status: String,
                        details: Option[String],
                        time: DateTime,
                        deployId: Long)

case class VerificationForCreate (status: String,
                                   details: Option[String])

