package com.timgroup.play_bonecp_tucker

import com.timgroup.tucker.info.{Status, Report, Component}
import com.jolbox.bonecp.{BoneCPConfig, Statistics}

class DataSourceHealthComponent(dataSourceName: String, config: BoneCPConfig, statistics: Statistics)
  extends Component("BoneCp-" + dataSourceName, "%s DB Connection Pool (%s)".format(dataSourceName, config.getJdbcUrl)) {

  override def getReport: Report = {
    val leasedConnections = statistics.getTotalLeased
    val totalConnections = statistics.getTotalCreatedConnections
    val maxConnections = config.getMaxConnectionsPerPartition * config.getPartitionCount

    val status = if (maxConnections - leasedConnections < 10) {
      Status.CRITICAL
    } else if (maxConnections - leasedConnections < 20) {
      Status.WARNING
    } else {
      Status.OK
    }

    new Report(status, "%s in use of %s (max %s)".format(leasedConnections, totalConnections, maxConnections))
  }
}
