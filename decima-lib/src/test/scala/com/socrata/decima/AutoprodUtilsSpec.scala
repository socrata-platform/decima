package com.socrata.decima

import com.socrata.decima.models.{AutoprodInfo, Deploy, S3BuildInfo}
import com.socrata.decima.util.AutoprodUtils
import org.json4s.JsonDSL._
import org.json4s.jackson.JsonMethods._
import org.scalatest.WordSpec

class AutoprodUtilsSpec extends WordSpec {
  def createAutoprodInfo(buildId: Option[String]): AutoprodInfo = {
    AutoprodInfo("service", "service-release", "staging", buildId, "an engineer", "autoprod:deploy")
  }

  def createBuildInfo(): S3BuildInfo = {
    val info = new S3BuildInfo()
    info.setService("service")
    info.setVersion("1.2.3")
    info.setService_sha("asdfasdf")
    info.setConfiguration("http://jenkins...")
    info.s3Url = "s3://bucket/key"
    info
  }

  def createBuildId(id: String): String = s"service/$id"

  val autoprodInfo = createAutoprodInfo(None)
  val buildInfo = createBuildInfo()

  "The AutoprodUtils class" should {
    "combine info from Autoprod & an S3 bucket to make a deploy" in {
      val createdDeploy = AutoprodUtils.infoToDeployModel(autoprodInfo, buildInfo)
      val expectedConfig = ("build_configuration" -> buildInfo.configuration) ~
        ("s3_url" -> buildInfo.s3Url)
      assert(createdDeploy.configuration.get == compact(render(expectedConfig)))
      assert(createdDeploy.getClass == classOf[Deploy])
    }

    "be able to sort a list of s3 keys and get the latest artifact" in {
      val expectedLatest = "4332_1"
      val buildIds = Seq(
        createBuildId("1234_32"),
        createBuildId("4312_23"),
        createBuildId(expectedLatest),
        createBuildId("1_1234"),
        createBuildId("9_12"),
        createBuildId("1234_32")
      )
      val latest = AutoprodUtils.getLatestBuild(buildIds)
      latest equals expectedLatest
    }
  }
}
