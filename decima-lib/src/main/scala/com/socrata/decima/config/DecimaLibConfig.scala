package com.socrata.decima.config

import com.amazonaws.ClientConfiguration
import com.amazonaws.auth._
import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.typesafe.config.Config
import grizzled.slf4j.Logging

class DecimaLibConfig(protected val config: Config) {
  val db = new DbConfig(config.getConfig("c3p0"))
  val aws = new AwsConfig(config.getConfig("aws"))
}

class DbConfig(config: Config) {
  val jdbcUrl = config.getString("jdbcUrl")
  val user = config.getString("user")
  val password = config.getString("password")
}

class AwsConfig(config: Config) extends Logging {
  // Credentials for all aws services
  val credentials: AWSCredentialsProvider = {
    if (config.hasPath("profile")) {
      val profile = config.getString("profile")
      logger.info(s"Using the AWS credentials from profile: $profile.")
      new ProfileCredentialsProvider(profile)
    }
    else if (config.hasPath("accessKeyId") && config.hasPath("secretAccessKey")) {
      logger.info("Using the AWS credentials specified in the Decima config file.")
      logger.info(s"Access key id: ${config.getString("accessKeyId")}")
      new AWSCredentialsProvider {
        val creds = new BasicAWSCredentials(config.getString("accessKeyId"), config.getString("secretAccessKey"))
        override def refresh(): Unit = {} // Noop
        override def getCredentials: AWSCredentials = creds
      }
    } else {
      logger.info("AWS credentials not supplied, defaulting to the default AWS Credentials Provider.")
      new DefaultAWSCredentialsProviderChain()
    }
  }

  // Client configuration for all aws services
  val clientConfig: ClientConfiguration = {
    val client = new ClientConfiguration()
    if (sys.env.contains("http_proxy")) {
      val proxyVar = sys.env("http_proxy")
      logger.info(s"Found proxy $proxyVar, attempting to set the client configuration")
      val proxyArgs = proxyVar.split("://").last.split(":")
      val proxyHost = proxyArgs.head
      val proxyPort = proxyArgs.last.toInt
      client.setProxyHost(proxyHost)
      client.setProxyPort(proxyPort)
    }
    client
  }
  val s3 = new S3Config(config.getConfig("s3"))
}

class S3Config(config: Config) {
  val bucketName = config.getString("bucketName")
}
