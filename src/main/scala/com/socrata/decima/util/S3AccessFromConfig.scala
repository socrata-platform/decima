package com.socrata.decima.util

import com.amazonaws.ClientConfiguration
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.s3.AmazonS3Client

object S3AccessFromConfig {
  def apply(s3Config: S3Config): AmazonS3Client = {
    val credentials = new BasicAWSCredentials(s3Config.accessKeyId, s3Config.secretAccessKey)
    if(sys.env.contains("http_proxy")) {
      val proxyArgs = sys.env("http_proxy").replace("http://", "").split(":")
      val proxyHost = proxyArgs.head
      val proxyPort = proxyArgs.last.toInt
      val clientConfiguration = new ClientConfiguration().withProxyHost(proxyHost).withProxyPort(proxyPort)
      new AmazonS3Client(credentials, clientConfiguration)
    } else {
      new AmazonS3Client(credentials)
    }
  }
}
