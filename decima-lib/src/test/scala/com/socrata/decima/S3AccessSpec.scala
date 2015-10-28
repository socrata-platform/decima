package com.socrata.decima

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.{ListObjectsRequest, ObjectListing}
import com.socrata.decima.data_access.S3Access
import org.scalamock.scalatest.MockFactory
import org.scalatest._

import scala.collection.JavaConverters._

class S3AccessSpec extends WordSpec with Matchers with MockFactory {

  def withMocks(f: (AmazonS3, S3Access) => Unit) = {
    val s3Client = mock[AmazonS3]
    val s3Access = new S3Access(s3Client, "bucket")
    f(s3Client, s3Access)
  }

  def createObjectListing(truncated: Boolean): ObjectListing = {
    val objectListing = new ObjectListing()
    objectListing.setCommonPrefixes(List("CoreServer/12345_12",
                                          "CoreServer/14567_16",
                                          "CoreServer/127_125",
                                          "CoreServer/62345_132").asJava)
    objectListing.setTruncated(truncated)
    objectListing
  }

  "The S3Access Class" should {
    "list out the build IDs for an S3 project" in {
      withMocks{ case (s3Client, s3Access) =>
        (s3Client.listObjects(_: ListObjectsRequest)) expects * returning createObjectListing(false)
        val buildPaths = s3Access.listBuildPaths("CoreServer")
        buildPaths should have length(4)
      }
    }

    "query the api again if there are more build IDs on the path" in {
      withMocks{ case (s3Client, s3Access) =>
        inOrder((s3Client.listObjects(_: ListObjectsRequest)) expects(*) returning createObjectListing(true),
                (s3Client.listNextBatchOfObjects(_: ObjectListing)) expects(*) returning createObjectListing(false))
        val buildPaths = s3Access.listBuildPaths("CoreServer")
        buildPaths should have length(8)
      }
    }

    "download and parse build info for a service" in pending
    "throw an exception if it fails to download or parse the build_info.yml" in pending

  }
}
