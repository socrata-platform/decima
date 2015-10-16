package com.socrata.decima.config

import com.typesafe.config.Config

class DecimaPollerConfig(config: Config) extends DecimaLibConfig(config) {
  override val aws = new PollerAwsConfig(config.getConfig("aws"))
}

class PollerAwsConfig(config: Config) extends AwsConfig(config) {
  val sqs = new SqsConfig(config.getConfig("sqs"))
}

class SqsConfig(config: Config) {
  val baseEndpoint = config.getString("baseEndpoint")
  val queueName = config.getString("queueName")
  val messagesPerPoll = config.getInt("messagesPerPoll")
  val pollTimeout = config.getInt("pollTimeout")
  val visibilityTimeout = config.getInt("visibilityTimeout")
}
