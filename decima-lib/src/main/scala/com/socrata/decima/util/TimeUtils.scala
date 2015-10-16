package com.socrata.decima.util

import java.sql.Timestamp
import org.joda.time.{DateTime, DateTimeZone}

object TimeUtils {

  def toJodaDateTime(timestamp:Timestamp): DateTime = {
    val timeZone = DateTimeZone.UTC
    new DateTime(timestamp.getTime, timeZone)
  }

  def toSqlTimestamp(jodaTime: DateTime): Timestamp = {
    new java.sql.Timestamp(jodaTime.getMillis)
  }

  def now: DateTime = DateTime.now(DateTimeZone.UTC)
}
