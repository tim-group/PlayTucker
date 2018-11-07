package com.timgroup.play_bonecp_tucker

import com.codahale.metrics.{Gauge, MetricRegistry}
import com.jolbox.bonecp.{BoneCPConfig, Statistics}
import com.timgroup.tucker.info.{Component, Report, Status}
import play.Logger

import scala.util.control.NonFatal

class DataSourceHealthComponent(dataSourceName: String, config: BoneCPConfig, statistics: Statistics)
  extends Component("BoneCp-" + dataSourceName, "%s DB Connection Pool usage".format(dataSourceName)) {

  def registerMetrics(metricRegistry: MetricRegistry) {
    val prefix = s"database.bonecp.$dataSourceName."

    def gauge[A](name: String)(f: => A) = {
      metricRegistry.register(prefix + name, new Gauge[A] { def getValue: A = f })
    }

    try {
      gauge("CacheHitRatio")(statistics.getCacheHitRatio)
      gauge("CacheHits")(statistics.getCacheHits)
      gauge("CacheMiss")(statistics.getCacheMiss)
      gauge("ConnectionsRequested")(statistics.getConnectionsRequested)
      gauge("ConnectionWaitTimeAvg")(statistics.getConnectionWaitTimeAvg)
      gauge("CumulativeConnectionWaitTime")(statistics.getCumulativeConnectionWaitTime)
      gauge("CumulativeStatementExecutionTime")(statistics.getCumulativeStatementExecutionTime)
      gauge("CumulativeStatementPrepareTime")(statistics.getCumulativeStatementPrepareTime)
      gauge("StatementExecuteTimeAvg")(statistics.getStatementExecuteTimeAvg)
      gauge("StatementPrepareTimeAvg")(statistics.getStatementPrepareTimeAvg)
      gauge("StatementsCached")(statistics.getStatementsCached)
      gauge("StatementsExecuted")(statistics.getStatementsExecuted)
      gauge("StatementsPrepared")(statistics.getStatementsPrepared)
      gauge("TotalCreatedConnections")(statistics.getTotalCreatedConnections)
      gauge("TotalFree")(statistics.getTotalFree)
      gauge("TotalLeased")(statistics.getTotalLeased)
    } catch {
      case NonFatal(e) => Logger.error("Error registering metrics for CP datasource " + dataSourceName, e)
    }
  }

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
