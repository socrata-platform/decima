package com.socrata.decima

import akka.actor.{ActorSystem, Props}
import com.amazonaws.services.sqs.AmazonSQSAsyncClient
import com.socrata.decima.actors.Reaper.WatchMe
import com.socrata.decima.actors.SqsMessageReader.PollDeploysMessage
import com.socrata.decima.actors._
import com.socrata.decima.config.DecimaPollerConfig
import com.socrata.decima.data_access.DeploymentAccessWithPostgres
import com.socrata.decima.database.{ActualPostgresDriver, DeploymentDAO}
import com.socrata.decima.util.DataSourceFromConfig
import com.typesafe.config.ConfigFactory

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.slick.jdbc.JdbcBackend._

object Poller extends App {
  // Load config
  val config = new DecimaPollerConfig(ConfigFactory.load)

  // Setup database clients and data sources for actors
  val cpds = DataSourceFromConfig(config.db)
  val db = Database.forDataSource(cpds)
  val deployAccess = new DeploymentAccessWithPostgres(db, new DeploymentDAO() with ActualPostgresDriver)

  // Setup sqs client to communicate with AWS
  val sqsClient = new AmazonSQSAsyncClient(config.aws.credentials, config.aws.clientConfig)
  sqsClient.setEndpoint(config.aws.sqs.baseEndpoint)

  // Setup ActorSystem and actors
  val system = ActorSystem("DecimaSystem")
  val reaper = system.actorOf(Props(new Reaper), "reaper")
  val messageFinalizer = system.actorOf(Props(new SqsMessageFinalizer(sqsClient, config.aws.sqs)), "finalizer")
  reaper ! WatchMe(messageFinalizer)
  val deployConsumer = system.actorOf(Props(new DeployConsumer(deployAccess, messageFinalizer)), "consumer")
  reaper ! WatchMe(deployConsumer)
  val messageReader = system.actorOf(
    Props(new SqsMessageReader(sqsClient, config.aws.sqs, deployConsumer, messageFinalizer)), "reader")
  reaper ! WatchMe(messageReader)

  // Kickoff polling
  system.scheduler.scheduleOnce(0.seconds, messageReader, PollDeploysMessage(messageReader))
}
