package com.socrata.decima.services

import com.socrata.decima.BuildInfo

/**
 * Created by michaelbrown on 5/12/15.
 */
class VersionService extends DecimaStack {
  get("/") {
    logger.info("Version request: " + BuildInfo.version)
    BuildInfo.toMap
  }
}
