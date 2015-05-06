
package com.socrata.decima.util

import java.sql.Timestamp

import org.joda.time.format.ISODateTimeFormat
import org.joda.time.{DateTime, DateTimeZone}

object TimeUtils {

  def toJodaTime(timestamp:Timestamp)= {
    val timeZone = DateTimeZone.UTC
    new DateTime(timestamp.getTime, timeZone)
  }

  def asTimestamp(jodaTime: DateTime): Timestamp = {
    new java.sql.Timestamp(jodaTime.getMillis)
  }

  def now = DateTime.now(DateTimeZone.UTC)
}
