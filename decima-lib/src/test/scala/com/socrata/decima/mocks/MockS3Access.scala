package com.socrata.decima.mocks

import com.socrata.decima.data_access.S3AccessBase
import com.socrata.decima.models.S3BuildInfo

class MockS3Access extends S3AccessBase {
  override def listBuildPaths(project: String): List[String] = {
    (1 to 100).toList.map(id =>
      s"$project/${1234000 + id}_$id"
    )
  }

  override def getBuildInfo(project: String, buildId: String): S3BuildInfo = {
    val info = new S3BuildInfo()
    info.configuration = "mock configuration"
    info.s3Url = s"s3://build/$project/$buildId/build_info.yml"
    info.service = project
    info.service_sha = "asdfasdf"
    info.version = "1.2.3"
    info
  }

}
