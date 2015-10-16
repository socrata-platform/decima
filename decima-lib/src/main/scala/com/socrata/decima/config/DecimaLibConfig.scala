package com.socrata.decima.config

import com.amazonaws.ClientConfiguration
import com.amazonaws.auth.{AWSCredentials, BasicAWSCredentials, AWSCredentialsProvider, DefaultAWSCredentialsProviderChain}
import com.typesafe.config.Config

class DecimaLibConfig(protected val config: Config) {
  val db = new DbConfig(config.getConfig("c3p0"))
  val aws = new AwsConfig(config.getConfig("aws"))
}

class DbConfig(config: Config) {
  val jdbcUrl = config.getString("jdbcUrl")
  val user = config.getString("user")
  val password = config.getString("password")
}

class AwsConfig(config: Config) {
  // Credentials for all aws services
  val credentials: AWSCredentialsProvider = {
    if (config.hasPath("accessKeyId") && config.hasPath("secretAccessKey")) {
      new AWSCredentialsProvider {
        val creds = new BasicAWSCredentials(config.getString("accessKeyId"), config.getString("secretAccessKey"))

        override def refresh(): Unit = {} // Noop
        override def getCredentials: AWSCredentials = creds
      }

    } else {
      new DefaultAWSCredentialsProviderChain()
    }
  }
  // Client configuration for all aws services
  val clientConfig: ClientConfiguration = {
    val client = new ClientConfiguration()
    if (sys.env.contains("http_proxy")) {
      val proxyVar = sys.env("http_proxy")
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
