package com.timgroup.play_bonecp_tucker

import com.jolbox.bonecp.{BoneCP, BoneCPDataSource}
import com.timgroup.tucker.info.component.DatabaseConnectionComponent
import com.timgroup.tucker.info.component.DatabaseConnectionComponent.ConnectionProvider
import java.sql.Connection
import javax.sql.DataSource
import play.api.{Logger, Application, Plugin}

class PlayBoneCpTuckerPlugin(application: Application) extends Plugin {

  override def onStart() {
    import com.timgroup.play_tucker.PlayTuckerPlugin

    Logger.info("PlayBoneCpTuckerPlugin started")
    val tucker = play.api.Play.current.plugin[PlayTuckerPlugin].get

    components.foreach(tucker.addComponent)
  }

  override def onStop() {
    Logger.info("PlayBoneCpTuckerPlugin stopped")
  }

  def components = {
    import play.api.db.BoneCPPlugin

    val boneCp = play.api.Play.current.plugin[BoneCPPlugin].get

    boneCp.api.datasources.flatMap {
      case (datasource: BoneCPDataSource, datasourceName: String) =>
        val pool: BoneCP = datasource.getPool
        enableStatisicsViaReflection(pool)

        Seq(new DataSourceHealthComponent(datasourceName, pool.getConfig, pool.getStatistics),
            new DatabaseConnectionComponent("Connectivity-" + datasourceName,
                                            "%s DB Connectivity (%s)".format(datasourceName, pool.getConfig.getJdbcUrl),
                                            connectionProviderFrom(datasource)))
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
}
