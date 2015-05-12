package com.socrata.decima.services

import com.socrata.decima.BuildInfo

class VersionService extends DecimaStack {
  get("/") {
    logger.info("Version request: " + BuildInfo.version)
    BuildInfo.toMap
  }
}
