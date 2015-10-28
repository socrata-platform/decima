package com.socrata.decima.util

import org.json4s.Formats

object JsonFormats {
  val Formats: Formats = org.json4s.DefaultFormats ++ org.json4s.ext.JodaTimeSerializers.all
}
