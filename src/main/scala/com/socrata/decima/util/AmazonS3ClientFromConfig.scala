package com.socrata.decima.util

import com.amazonaws.ClientConfiguration
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.s3.AmazonS3Client
import grizzled.slf4j.Logging

object AmazonS3ClientFromConfig extends Logging {
  def apply(s3Config: S3Config): AmazonS3Client = {
    val credentials = new BasicAWSCredentials(s3Config.accessKeyId, s3Config.secretAccessKey)
    if (sys.env.contains("http_proxy")) {
      val proxyVar = sys.env("http_proxy")
      logger.info(s"Creating AmazonS3Client with proxy $proxyVar.")
      val proxyArgs = proxyVar.split("://").last.split(":")
      val proxyHost = proxyArgs.head
      val proxyPort = proxyArgs.last.toInt
      val clientConfiguration = new ClientConfiguration()
                                      .withProxyHost(proxyHost)
                                      .withProxyPort(proxyPort)
      new AmazonS3Client(credentials, clientConfiguration)
    } else {
      logger.info(s"Creating basic AmazonS3Client.")
      new AmazonS3Client(credentials)
    }
  }
}
