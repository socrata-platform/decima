package com.socrata.decima.database

import com.socrata.decima.database.tables.DeployTable
import com.socrata.decima.models._
import com.socrata.decima.util.TimeUtils

class DeployDAO extends DeployTable {
  self: DatabaseDriver =>

  import driver.simple._

  def createDeploy(deploy: DeployForCreate)(implicit session: Session): Either[Exception, Deploy] = {
    session.withTransaction {
      val now = TimeUtils.now
      val newId = (deployTable returning deployTable.map(_.id)) += DeployRow(0,
                                                                              deploy.service,
                                                                              deploy.environment,
                                                                              deploy.version,
                                                                              deploy.git,
                                                                              deploy.deployedBy,
                                                                              TimeUtils.asTimestamp(now))
      val newDeploy = DeployCompiledQueries.lookup(newId).run.headOption
      newDeploy match {
        case Some(d) => Right(rowToModelDeploy(d))
        case None => Left(new RuntimeException("Unable to create deploy"))
      }
    }
  }

  def deploymentHistory(n: Int)(implicit session: Session): Seq[Deploy] = {
    val res = DeployCompiledQueries.deploymentHistory(n).run
    res.map(d => rowToModelDeploy(d))
  }

  def currentDeployment(implicit session:Session): Seq[Deploy] = {
    val res = DeployCompiledQueries.currentDeployment.list
    res.map(d => rowToModelDeploy(d))
  }
}
