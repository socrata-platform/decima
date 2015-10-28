package com.socrata.decima.database.tables

import java.sql.Timestamp

import com.socrata.decima.database.DatabaseDriver

import scala.slick.jdbc.{GetResult, StaticQuery}

trait DeployTable {
  self: DatabaseDriver => ()
  import self.driver.simple._ // scalastyle:ignore import.grouping

  case class DeployRow(id: Long,
                       service: String,
                       environment: String,
                       version: String,
                       dockerTag: Option[String],
                       serviceSha: String,
                       dockerSha: Option[String],
                       configuration: Option[String],
                       deployedBy: String,
                       deployMethod: String,
                       deployedAt: Timestamp)

  class Deploys(tag: Tag) extends Table[DeployRow](tag, "deploys") {
    // scalastyle:off
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc) // primary key column
    def service = column[String]("service")
    def environment = column[String]("environment")
    def version = column[String]("version")
    def dockerTag = column[Option[String]]("docker_tag")
    def serviceSha = column[String]("service_sha")
    def dockerSha = column[Option[String]]("docker_sha")
    def configuration = column[Option[String]]("configuration", O.DBType("text"))
    def deployedBy = column[String]("deployed_by")
    def deployMethod = column[String]("deploy_method")
    def deployedAt = column[Timestamp]("deployed_at")
    def * = ( id,
              service,
              environment,
              version,
              dockerTag,
              serviceSha,
              dockerSha,
              configuration,
              deployedBy,
              deployMethod,
              deployedAt) <> (DeployRow.tupled, DeployRow.unapply)
    // scalastyle:on
  }

  val deployTable = TableQuery[Deploys]

  implicit val getDeployResult = GetResult(r => DeployRow(r.<<, r.<<, r.<<, r.<<, r.<<,
                                                          r.<<, r.<<, r.<<, r.<<, r.<<, r.<<))

  val currentDeploymentQuery = StaticQuery.queryNA[DeployRow]( """
                                  select a.id, a.service, a.environment,
                                    b.version, b.docker_tag, b.service_sha,
                                    b.docker_sha, b.configuration, b.deployed_by,
                                    b.deploy_method, b.deployed_at
                                  from (
                                    select distinct deploys.service, deploys.environment, max(deploys.id) as id
                                    from deploys
                                    group by deploys.service, deploys.environment) a,
                                  deploys b
                                  where a.id = b.id""")

  object DeployCompiledQueries {
    private def lookupByIdQuery(id: Column[Long]) = deployTable.filter(d => d.id === id)

    val lookup = Compiled(lookupByIdQuery _)
  }
}
