package com.timgroup.play_bonecp_tucker

import com.timgroup.tucker.info.{Status, Component, Report}
import com.jolbox.bonecp.{BoneCPConfig, Statistics}
import com.codahale.metrics.{MetricFilter, MetricRegistry}
import nl.grons.metrics.scala.MetricBuilder
import play.Logger
import scala.util.control.NonFatal

object TuckerMetrics {
  val metricRegistry = new MetricRegistry
  def clearMetricRegistry(): Unit = metricRegistry.removeMatching(MetricFilter.ALL)
}

class DataSourceHealthComponent(dataSourceName: String, config: BoneCPConfig, statistics: Statistics)
  extends Component("BoneCp-" + dataSourceName, "%s DB Connection Pool usage (%s)".format(dataSourceName, config.getJdbcUrl)) {

  val metricBuilder = new MetricBuilder(this.getClass, TuckerMetrics.metricRegistry)

  try {
    gauge("CacheHitRatio", () => statistics.getCacheHitRatio)
    gauge("CacheHits", () => statistics.getCacheHits)
    gauge("CacheMiss", () => statistics.getCacheMiss)
    gauge("ConnectionsRequested", () => statistics.getConnectionsRequested)
    gauge("ConnectionWaitTimeAvg", () => statistics.getConnectionWaitTimeAvg)
    gauge("CumulativeConnectionWaitTime", () => statistics.getCumulativeConnectionWaitTime)
    gauge("CumulativeStatementExecutionTime", () => statistics.getCumulativeStatementExecutionTime)
    gauge("CumulativeStatementPrepareTime", () => statistics.getCumulativeStatementPrepareTime)
    gauge("StatementExecuteTimeAvg", () => statistics.getStatementExecuteTimeAvg)
    gauge("StatementPrepareTimeAvg", () => statistics.getStatementPrepareTimeAvg)
    gauge("StatementsCached", () => statistics.getStatementsCached)
    gauge("StatementsExecuted", () => statistics.getStatementsExecuted)
    gauge("StatementsPrepared", () => statistics.getStatementsPrepared)
    gauge("TotalCreatedConnections", () => statistics.getTotalCreatedConnections)
    gauge("TotalFree", () => statistics.getTotalFree)
    gauge("TotalLeased", () => statistics.getTotalLeased)
  } catch {
    case NonFatal(e) => Logger.error("Error registering metrics for CP datasource " + dataSourceName, e)
  }

  private def gauge[T <: Any](name: String, f: () => T) = {
    metricBuilder.gauge("database.bonecp."+name,dataSourceName)(f)
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
