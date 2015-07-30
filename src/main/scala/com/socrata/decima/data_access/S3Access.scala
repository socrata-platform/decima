package com.socrata.decima.data_access

import awscala.s3.S3
import com.socrata.decima.models.S3BuildInfo
import grizzled.slf4j.Logging
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.constructor.Constructor

trait S3AccessBase {
  def listBuildPaths(project: String): List[String]
  def getBuildInfo(project: String, buildId: String): S3BuildInfo
}

class S3Access(s3: S3, bucketName: String) extends S3AccessBase with Logging {
  lazy val bucket = s3.bucket(bucketName)
  lazy val yaml = new Yaml(new Constructor(classOf[S3BuildInfo]))

  /**
   * Get the list of build IDs from S3 of a project
   * This does a little processing:
   *  - List out everything under $project/
   *  - Filters out everything that isn't a prefix (S3 objects aren't builds)
   * @param project: the prefix of the project to use in finding the build
   * @return a list of build prefixes, e.g. ["CoreServer/12341234_12", ...]
   */
  override def listBuildPaths(project: String): List[String] = {
    val builds = s3.ls(bucket.get, s"$project/").filter(_.isLeft)
    if (builds.isEmpty) throw new RuntimeException(s"Unable to find any builds under $project")
    builds.map(_.left.get).toList
  }

  /**
   * Retrieves the build_info.yml file from S3 and parses it into an instance of S3BuildInfo
   * @param project
   * @param buildId
   * @return
   */
  override def getBuildInfo(project: String, buildId: String): S3BuildInfo = {
    val fileKey = s"$project/$buildId/build_info.yml"
    val s3Url = s"s3://$bucketName/$fileKey"
    logger.info(s"Retrieving build info from $s3Url")
    val buildInfoObject = s3.get(bucket.get, fileKey)
    if (buildInfoObject.isEmpty) throw new RuntimeException(s"Unable to find the build info at $s3Url")
    try {
      val buildInfo = yaml.load(buildInfoObject.get.content).asInstanceOf[S3BuildInfo]
      buildInfo.s3Url = s3Url
      buildInfo
    } catch {
      case e: Exception =>
        throw new RuntimeException("Unable to parse build_info.yml file (it may be incorrectly formatted):\n"
          + e.getMessage)
    }
  }
}
