package com.socrata.decima.data_access

import com.amazonaws.services.s3._
import com.amazonaws.services.s3.model.{ListObjectsRequest, ObjectListing}
import com.socrata.decima.models.S3BuildInfo
import grizzled.slf4j.Logging
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.constructor.Constructor

import scala.collection.JavaConverters._

trait S3AccessBase {
  def listBuildPaths(project: String): Seq[String]
  def getBuildInfo(project: String, buildId: String): S3BuildInfo
}

class S3Access(s3: AmazonS3Client, bucketName: String) extends S3AccessBase with Logging {
  lazy val yaml = new Yaml(new Constructor(classOf[S3BuildInfo]))

  /**
   * Get the list of build IDs from S3 of a project
   * This does a little processing:
   *  - List out everything under $project/
   *  - Filters out everything that isn't a prefix (S3 objects aren't builds)
   * @param project: the prefix of the project to use in finding the build
   * @return a list of build prefixes, e.g. ["CoreServer/12341234_12", ...]
   */
  override def listBuildPaths(project: String): Seq[String] = {
    var objectListing = s3.listObjects(new ListObjectsRequest()
                                            .withBucketName(bucketName)
                                            .withPrefix(s"$project/")
                                            .withDelimiter("/"))
    var prefixes = getBuildPrefixes(objectListing)
    while(objectListing.isTruncated) {
      objectListing = s3.listNextBatchOfObjects(objectListing)
      prefixes ++= getBuildPrefixes(objectListing)
    }
    if (prefixes.isEmpty) throw new RuntimeException(s"Unable to find any builds under $project")
    prefixes
  }

  private def getBuildPrefixes(objectListing: ObjectListing): Seq[String] = {
    objectListing.getCommonPrefixes.asScala
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
    try {
      val buildInfoObject = s3.getObject(bucketName, fileKey)
      val inputStream = buildInfoObject.getObjectContent
      val buildInfo = yaml.load(inputStream).asInstanceOf[S3BuildInfo]
      buildInfoObject.close()
      buildInfo.s3Url = s3Url
      buildInfo
    } catch {
      case e: Exception =>
        throw new RuntimeException(s"Unable to download and parse build_info.yml at $s3Url. Is it present?\n"
          + e.getMessage)
    }
  }
}
