package com.socrata.decima.util

import awscala._
import awscala.s3._

object S3AccessFromConfig {
  def apply(s3Config: S3Config): S3 = {
    implicit val region = Region.US_WEST_2
    S3(s3Config.accessKeyId,
        s3Config.secretAccessKey)
  }
}
