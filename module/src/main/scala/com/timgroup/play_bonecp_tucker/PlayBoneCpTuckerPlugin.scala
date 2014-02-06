package com.timgroup.play_bonecp_tucker

import com.jolbox.bonecp.{BoneCP, BoneCPDataSource}
import com.timgroup.tucker.info.component.DatabaseConnectionComponent
import com.timgroup.tucker.info.component.DatabaseConnectionComponent.ConnectionProvider
import java.sql.Connection
import javax.sql.DataSource

object PlayBoneCpTuckerPlugin {
  def components = {
    import play.api.Play.current
    import com.typesafe.plugin.use
    import play.api.db.BoneCPPlugin

    val boneCp = use[BoneCPPlugin]

    boneCp.api.datasources.flatMap {
      case (datasource: BoneCPDataSource, datasourceName: String) =>  {
        val pool = getPoolViaReflection(datasource)
        enableStatisicsViaReflection(pool)

        Seq(new DataSourceHealthComponent(datasourceName, pool.getConfig, pool.getStatistics),
            new DatabaseConnectionComponent("Connectivity-" + datasourceName,
                                            "%s DB Connectivity (%s)".format(datasourceName, pool.getConfig.getJdbcUrl),
                                            connectionProviderFrom(datasource)))
      }
    }
  }

  private def connectionProviderFrom(datasource: DataSource) = new ConnectionProvider {
    def getConnection: Connection = datasource.getConnection
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


