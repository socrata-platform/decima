package com.socrata.decima.database.tables

import java.sql.Timestamp

import com.socrata.decima.database.DatabaseDriver

trait VerificationTable extends DeployTable {
  self: DatabaseDriver =>
  import self.driver.simple._

  case class VerificationRow( id: Long,
                               status: String,
                               details: Option[String],
                               time: Timestamp,
                               deployId: Long)

  class Verifications(tag: Tag) extends Table[VerificationRow](tag, "verifications") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc) // primary key for verifications
    def status = column[String]("status")
    def details = column[Option[String]]("details", O.DBType("text"))
    def time = column[Timestamp]("time")
    def deployId = column[Long]("deploy_id") // foreign key, references related deploy event

    def * = (id, status, details, time, deployId) <> (VerificationRow.tupled, VerificationRow.unapply)

    def deploy = foreignKey("verifications_deploy_id_fkey", deployId, self.deployTable)(_.id)
  }

  def verificationTable = TableQuery[Verifications]

  object VerificationCompiledQueries {
    private def lookupByIdQuery(id: Column[Long]) = verificationTable.filter(v => v.id === id)

    val lookup = Compiled(lookupByIdQuery _)
  }
}
