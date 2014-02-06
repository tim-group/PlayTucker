package com.timgroup.play_bonecp_tucker

import com.timgroup.tucker.info.{Status, Report, Component}
import com.jolbox.bonecp.{BoneCPConfig, Statistics}

class DataSourceHealthComponent(dataSourceName: String, config: BoneCPConfig, statistics: Statistics)
  extends Component("BoneCp-" + dataSourceName, "BoneCp-" + dataSourceName) {

  override def getReport: Report = {
    val leasedConnections: Int = statistics.getTotalLeased
    val totalConnections: Int = statistics.getTotalCreatedConnections
    val maxConnections: Int = config.getMaxConnectionsPerPartition

    new Report(if (maxConnections - leasedConnections < 10) Status.CRITICAL else Status.OK,
      "%s in use of %s (max %s)".format(leasedConnections, totalConnections, maxConnections))
  }
}
