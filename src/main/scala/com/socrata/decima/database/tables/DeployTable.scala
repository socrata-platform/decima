package com.socrata.decima.database.tables

import com.socrata.decima.database.DatabaseDriver
import java.sql.Timestamp

import com.socrata.decima.models.Deploy
import com.socrata.decima.util.TimeUtils

import scala.slick.jdbc.{GetResult, StaticQuery}

trait DeployTable {
  self: DatabaseDriver =>

  import self.driver.simple._

  case class DeployRow(id: Int,
                    service: String,
                    environment: String,
                    version: String,
                    git : Option[String],
                    deployedBy: String,
                    deployedAt: Timestamp)

  class Deploys(tag: Tag) extends Table[DeployRow](tag, "deploys") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc) // primary key column
    def service = column[String]("service")
    def environment = column[String]("environment")
    def version = column[String]("version")
    def git = column[Option[String]]("git")
    def deployedBy = column[String]("deployed_by")
    def deployedAt = column[Timestamp]("deployed_at")

    def * = (id, service, environment, version, git, deployedBy, deployedAt) <> (DeployRow.tupled, DeployRow.unapply)

  }

  def rowToModelDeploy(row:DeployRow): Deploy = {
    Deploy(row.id, row.service, row.environment, row.version, row.git, row.deployedBy, TimeUtils.toJodaTime(row.deployedAt))
  }

  val deployTable = TableQuery[Deploys]

  object DeployCompiledQueries {
    private def lookupByIdQuery(id:Column[Int]) = deployTable.filter(d => d.id === id)
    val lookup = Compiled(lookupByIdQuery _)

    private def deploymentHistoryQuery(n:ConstColumn[Long]) = deployTable.sortBy(_.deployedAt).take(n)
    val deploymentHistory = Compiled(deploymentHistoryQuery _)

    implicit val getDeployResult = GetResult(r => DeployRow(r.<<, r.<<, r.<<, r.<<, r.<<, r.<<, r.<<))
    def currentDeployment = {
      StaticQuery.queryNA[DeployRow]("""
                                  select a.id, a.service, a.environment, b.version, b.git, b.deployed_by, b.deployed_at
                                  from (
                                    select distinct deploys.service, deploys.environment, max(deploys.id) as id
                                    from deploys
                                    group by deploys.service, deploys.environment) a,
                                  deploys b
                                  where a.id = b.id""")
    }
  }

}
