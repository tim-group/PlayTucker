package com.timgroup.play_bonecp_tucker

import com.codahale.metrics.MetricRegistry
import com.jolbox.bonecp.{BoneCPConfig, Statistics}
import com.timgroup.tucker.info.{Component, Report, Status}
import nl.grons.metrics.scala.{MetricBuilder, MetricName}
import play.Logger

import scala.util.control.NonFatal


class DataSourceHealthComponent(dataSourceName: String, config: BoneCPConfig, statistics: Statistics)
  extends Component("BoneCp-" + dataSourceName, "%s DB Connection Pool usage".format(dataSourceName)) {

  def registerMetrics(metricRegistry: MetricRegistry) {
    val metricBuilder = new MetricBuilder(MetricName(s"database.bonecp.$dataSourceName"), metricRegistry)

    try {
      metricBuilder.gauge("CacheHitRatio")(statistics.getCacheHitRatio)
      metricBuilder.gauge("CacheHits")(statistics.getCacheHits)
      metricBuilder.gauge("CacheMiss")(statistics.getCacheMiss)
      metricBuilder.gauge("ConnectionsRequested")(statistics.getConnectionsRequested)
      metricBuilder.gauge("ConnectionWaitTimeAvg")(statistics.getConnectionWaitTimeAvg)
      metricBuilder.gauge("CumulativeConnectionWaitTime")(statistics.getCumulativeConnectionWaitTime)
      metricBuilder.gauge("CumulativeStatementExecutionTime")(statistics.getCumulativeStatementExecutionTime)
      metricBuilder.gauge("CumulativeStatementPrepareTime")(statistics.getCumulativeStatementPrepareTime)
      metricBuilder.gauge("StatementExecuteTimeAvg")(statistics.getStatementExecuteTimeAvg)
      metricBuilder.gauge("StatementPrepareTimeAvg")(statistics.getStatementPrepareTimeAvg)
      metricBuilder.gauge("StatementsCached")(statistics.getStatementsCached)
      metricBuilder.gauge("StatementsExecuted")(statistics.getStatementsExecuted)
      metricBuilder.gauge("StatementsPrepared")(statistics.getStatementsPrepared)
      metricBuilder.gauge("TotalCreatedConnections")(statistics.getTotalCreatedConnections)
      metricBuilder.gauge("TotalFree")(statistics.getTotalFree)
      metricBuilder.gauge("TotalLeased")(statistics.getTotalLeased)
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
