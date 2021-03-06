package com.timgroup.play_bonecp_tucker

import java.sql.Connection
import javax.sql.DataSource

import com.jolbox.bonecp.{BoneCP, BoneCPDataSource}
import com.timgroup.play_metrics_graphite.Metrics
import com.timgroup.play_tucker.PlayTuckerPlugin
import com.timgroup.tucker.info.component.DatabaseConnectionComponent
import com.timgroup.tucker.info.component.DatabaseConnectionComponent.ConnectionProvider
import play.api.{Application, Logger, Plugin}

object BoneCpMetrics extends Metrics

class PlayBoneCpTuckerPlugin(app: Application) extends Plugin {

  override def onStart() {
    BoneCpMetrics.start(app.configuration)

    val tucker = play.api.Play.current.plugin[PlayTuckerPlugin].get
    components.foreach(tucker.addComponent)
    Logger.info("PlayBoneCpTuckerPlugin started")
  }

  override def onStop() {
    BoneCpMetrics.stop()
    Logger.info("PlayBoneCpTuckerPlugin stopped")
  }

  private def components = {
    import play.api.db.BoneCPPlugin

    val boneCpPlugin = play.api.Play.current.plugin[BoneCPPlugin].get

    boneCpPlugin.api.datasources.flatMap {
      case (datasource: BoneCPDataSource, datasourceName: String) =>
        val pool: BoneCP = datasource.getPool
        enableStatisicsViaReflection(pool)

        val dataSourceHealthComponent = new DataSourceHealthComponent(datasourceName, pool.getConfig, pool.getStatistics)
        BoneCpMetrics.registry.foreach(dataSourceHealthComponent.registerMetrics)
        Seq(dataSourceHealthComponent,
            new DatabaseConnectionComponent("Connectivity-" + datasourceName,
                                            "%s DB Connectivity".format(datasourceName),
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
