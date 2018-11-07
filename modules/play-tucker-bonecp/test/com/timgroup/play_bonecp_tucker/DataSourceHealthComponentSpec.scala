package com.timgroup.play_bonecp_tucker

import com.codahale.metrics
import com.jolbox.bonecp.hooks.ConnectionHook
import com.jolbox.bonecp.{BoneCPConfigMBean, StatisticsMBean}
import com.timgroup.tucker.info.Status
import org.scalatest._

class DataSourceHealthComponentSpec extends FunSpec with MustMatchers {
  describe("The metrics registry") {
    it("is populated with working gauges") {
      val statistics = DummyStatistics(totalFree = 42)

      val component = new DataSourceHealthComponent("db", DummyConfig(), statistics)
      val registry = new metrics.MetricRegistry

      component.registerMetrics(registry)

      val keyName = "database.bonecp.db.TotalFree"
      registry.getGauges.get(keyName).getValue must be(42)
    }
  }

  describe("The datasource health") {
    it("shows the number of leased, created and max connections") {
      val config = DummyConfig(partitionCount = 1, maxConnectionsPerPartition = 50)
      val statistics = DummyStatistics(totalCreatedConnections = 20, totalLeased = 1)

      val component = new DataSourceHealthComponent("db", config, statistics)

      component.getReport.getValue must be ("1 in use of 20 (max 50)")
    }

    it("uses the maxConnectionsPerPartition x partitionCount as maximum") {
      val config = DummyConfig(partitionCount = 2, maxConnectionsPerPartition = 50)
      val statistics = DummyStatistics(totalCreatedConnections = 20, totalLeased = 1)

      val component = new DataSourceHealthComponent("db", config, statistics)

      component.getReport.getValue must be ("1 in use of 20 (max 100)")
    }

    it("shows ok when few connections are in use") {
      val config = DummyConfig(partitionCount = 1, maxConnectionsPerPartition = 50)
      val statistics = DummyStatistics(totalLeased = 1)

      val component = new DataSourceHealthComponent("db", config, statistics)

      component.getReport.getStatus must be (Status.OK)
    }

    it("shows critical when less that 10 connections are available") {
      val config = DummyConfig(partitionCount = 1, maxConnectionsPerPartition = 50)
      val statistics = DummyStatistics(totalLeased = 41)

      val component = new DataSourceHealthComponent("db", config, statistics)

      component.getReport.getStatus must be (Status.CRITICAL)
    }

    it("shows warning when less that 20 connections are available") {
      val config = DummyConfig(partitionCount = 1, maxConnectionsPerPartition = 50)
      val statistics = DummyStatistics(totalLeased = 31)

      val component = new DataSourceHealthComponent("db", config, statistics)

      component.getReport.getStatus must be (Status.WARNING)
    }
  }

  case class DummyConfig(partitionCount: Int = 0, maxConnectionsPerPartition: Int = 0) extends BoneCPConfigMBean {
    override def getPoolName: String = ???
    override def getMinConnectionsPerPartition: Int = ???
    override def getMaxConnectionsPerPartition: Int = maxConnectionsPerPartition
    override def getAcquireIncrement: Int = ???
    override def getPartitionCount: Int = partitionCount
    override def getJdbcUrl: String = ???
    override def getUsername: String = ???
    override def getIdleConnectionTestPeriodInMinutes: Long = ???
    override def getIdleMaxAgeInMinutes: Long = ???
    override def getConnectionTestStatement: String = ???
    override def getStatementsCacheSize: Int = ???
    override def getReleaseHelperThreads: Int = ???
    override def getStatementsCachedPerConnection: Int = ???
    override def getConnectionHook: ConnectionHook = ???
    override def getInitSQL: String = ???
    override def isLogStatementsEnabled: Boolean = ???
    override def getAcquireRetryDelayInMs: Long = ???
    override def isLazyInit: Boolean = ???
    override def isTransactionRecoveryEnabled: Boolean = ???
    override def getAcquireRetryAttempts: Int = ???
    override def getConnectionHookClassName: String = ???
    override def isDisableJMX: Boolean = ???
    override def getQueryExecuteTimeLimitInMs: Long = ???
    override def getPoolAvailabilityThreshold: Int = ???
    override def isDisableConnectionTracking: Boolean = ???
    override def getConnectionTimeoutInMs: Long = ???
    override def getCloseConnectionWatchTimeoutInMs: Long = ???
    override def getStatementReleaseHelperThreads: Int = ???
    override def getMaxConnectionAgeInSeconds: Long = ???
    override def getConfigFile: String = ???
    override def getServiceOrder: String = ???
    override def isStatisticsEnabled: Boolean = ???
  }

  case class DummyStatistics(totalLeased: Int = 0, totalFree: Int = 0, totalCreatedConnections: Int = 0) extends StatisticsMBean {
    override def getConnectionWaitTimeAvg: Double = 0
    override def getStatementExecuteTimeAvg: Double = 0
    override def getStatementPrepareTimeAvg: Double = 0
    override def getTotalLeased: Int = totalLeased
    override def getTotalFree: Int = totalFree
    override def getTotalCreatedConnections: Int = totalCreatedConnections
    override def getCacheHits: Long = 0
    override def getCacheMiss: Long = 0
    override def getStatementsCached: Long = 0
    override def getStatementsPrepared: Long = 0
    override def getConnectionsRequested: Long = 0
    override def getCumulativeConnectionWaitTime: Long = 0
    override def getCumulativeStatementExecutionTime: Long = 0
    override def getCumulativeStatementPrepareTime: Long = 0
    override def getCacheHitRatio: Double = 0
    override def getStatementsExecuted: Long = 0
    override def resetStats(): Unit = Unit
  }
}
