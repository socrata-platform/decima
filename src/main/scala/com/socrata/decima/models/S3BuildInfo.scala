package com.socrata.decima.models

/**
 * S3BuildInfo is used to parse a YAML file using Java's snakeyaml class
 * Because of this, we have to do sad things, like "service_sha" and defining
 * setters explicitly.
 *
 * We also have to include the 'service' tag because some build_info files contain
 * them. We don't use it for building the Deploy object though, because some builds
 * end up building more than 1 service (for projects that have multiple services in the
 * same Git repository).
 */

//scalastyle:off
class S3BuildInfo () {
  var service: String = null
  var version: String = null
  var service_sha: String = null
  var configuration: String = null
  var s3Url: String = null
  def setService(s: String): Unit = { service = s }
  def setVersion(s: String): Unit = { version = s }
  def setService_sha(s: String): Unit = { service_sha = s }
  def setConfiguration(s: String): Unit = { configuration = s }
}
