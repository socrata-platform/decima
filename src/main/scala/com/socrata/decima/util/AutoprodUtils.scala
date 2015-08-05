package com.socrata.decima.util

import com.socrata.decima.models._
import grizzled.slf4j.Logging
import org.json4s.JsonDSL._
import org.json4s.jackson.JsonMethods._

object AutoprodUtils extends Logging {

  def getLatestBuild(buildIds: Seq[String]): String = {
    buildIds.map(_.split("/").last).sortWith(numericSort).last
  }

  def infoToDeployModel(autoprodInfo: AutoprodInfo, buildInfo: S3BuildInfo): DeployForCreate = {
    val configuration = ("build_configuration" -> buildInfo.configuration) ~
                        ("s3_url" -> buildInfo.s3Url)
    DeployForCreate(autoprodInfo.service,
                    autoprodInfo.environment,
                    buildInfo.version,
                    None,
                    buildInfo.service_sha,
                    None,
                    Option(compact(render(configuration))),
                    autoprodInfo.deployedBy,
                    autoprodInfo.deployMethod)
  }

  private def numericSort(id1: String, id2: String): Boolean = {
    intFromBuildId(id1) < intFromBuildId(id2)
  }

  private def intFromBuildId(id: String): Int = id.split("_").head.toInt
}
