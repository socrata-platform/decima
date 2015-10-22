package com.socrata.decima

import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.auth.{BasicAWSCredentials, DefaultAWSCredentialsProviderChain}
import com.socrata.decima.config.AwsConfig
import com.typesafe.config.{Config, ConfigFactory}
import org.scalatest.{Matchers, WordSpec}

import scala.collection.JavaConverters._

class AwsConfigSpec extends WordSpec with Matchers {
  def createAwsConfig(map: Map[String, _ <: AnyRef]): Config = {
    ConfigFactory.parseMap(map.asJava)
      .withFallback(ConfigFactory.load.getConfig("aws"))
  }

  "The AwsConfig object" should {
    "be able to be created with defaults" in {
      val config = new AwsConfig(createAwsConfig(Map()))
      assert(config.credentials.getCredentials.getClass == classOf[BasicAWSCredentials])
    }

    "create using a profile" in {
      val config = new AwsConfig(createAwsConfig(Map("profile" -> "myProfile")))
      assert(config.credentials.getClass == classOf[ProfileCredentialsProvider])
    }

    "create using a default AWS credentials provider if nothing is provided" in {
      val config = ConfigFactory.parseMap(Map("s3.bucketName" -> "s3 bucket").asJava)
      val awsConfig = new AwsConfig(config)
      assert(awsConfig.credentials.getClass == classOf[DefaultAWSCredentialsProviderChain])
    }
  }
}
