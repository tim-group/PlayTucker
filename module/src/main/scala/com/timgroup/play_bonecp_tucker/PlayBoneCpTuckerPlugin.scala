package com.timgroup.play_bonecp_tucker

import com.timgroup.tucker.info.{Status, Report, Component}
import com.typesafe.plugin._
import com.jolbox.bonecp.{BoneCPConfig, Statistics, BoneCP, BoneCPDataSource}

object PlayBoneCpTuckerPlugin {
  def components = {
    import play.api.Play.current
    import com.typesafe.plugin.use
    import play.api.db.BoneCPPlugin

    val boneCp = use[BoneCPPlugin]

    boneCp.api.datasources.map {
      case (datasource: BoneCPDataSource, datasourceName: String) =>  {
        val pool = getPoolViaReflection(datasource)
        enableStatisicsViaReflection(pool)

        new DataSourceHealthComponent(datasourceName, pool.getConfig, pool.getStatistics)
      }
    }
  }

  private def enableStatisicsViaReflection(pool: BoneCP): Unit = {
    val statisticsEnabledField = pool.getClass.getDeclaredField("statisticsEnabled")
    statisticsEnabledField.setAccessible(true)
    statisticsEnabledField.setBoolean(pool, true)
  }

  private def getPoolViaReflection(datasource: BoneCPDataSource) = {
    val poolField = datasource.getClass.getDeclaredField("pool");
    poolField.setAccessible(true)
    poolField.get(datasource).asInstanceOf[BoneCP]
  }
}

class DataSourceHealthComponent(dataSourceName: String, config: BoneCPConfig, statistics: Statistics)
  extends Component("BoneCp-" + dataSourceName, "BoneCp-" + dataSourceName) {

  override def getReport: Report = new Report(Status.OK, "%s in use of %s (max %s)".format(
    statistics.getTotalLeased,
    statistics.getTotalCreatedConnections,
    config.getMaxConnectionsPerPartition))
}
