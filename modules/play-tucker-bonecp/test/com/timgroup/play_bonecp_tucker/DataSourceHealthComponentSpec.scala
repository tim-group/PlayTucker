package com.timgroup.play_bonecp_tucker

import java.util

import com.codahale.metrics
import com.codahale.metrics.{Gauge, MetricRegistry}
import com.jolbox.bonecp.{BoneCPConfig, Statistics}
import com.timgroup.tucker.info.Status
import org.mockito.BDDMockito._
import org.scalatest._
import org.scalatest.mock.MockitoSugar

import scala.collection.JavaConversions._

class DataSourceHealthComponentSpec extends path.FunSpec with MockitoSugar with MustMatchers {
  describe("The metrics registry") {
    it("is populated with working gauges") {
      val statistics: Statistics = mock[Statistics]
      given(statistics.getTotalFree).willReturn(42)

      val component = new DataSourceHealthComponent("db", mock[BoneCPConfig], statistics)
      val registry = new metrics.MetricRegistry

      component.registerMetrics(registry)

      val keyName = component.getClass.getName + ".database.bonecp.TotalFree.db"
      registry.getGauges.get(keyName).getValue must be(42)
    }
  }

  describe("The datasource health") {
    it("shows the number of leased, created and max connections") {
      val config = mock[BoneCPConfig]
      val statistics = mock[Statistics]

      given(config.getMaxConnectionsPerPartition).willReturn(50)
      given(config.getPartitionCount).willReturn(1)
      given(statistics.getTotalCreatedConnections).willReturn(20)
      given(statistics.getTotalLeased).willReturn(1)

      val component = new DataSourceHealthComponent("db", config, statistics)

      component.getReport.getValue must be ("1 in use of 20 (max 50)")
    }

    it("uses the maxConnectionsPerPartition x partitionCount as maximum") {
      val config = mock[BoneCPConfig]
      val statistics = mock[Statistics]

      given(config.getMaxConnectionsPerPartition).willReturn(50)
      given(config.getPartitionCount).willReturn(2)
      given(statistics.getTotalCreatedConnections).willReturn(20)
      given(statistics.getTotalLeased).willReturn(1)

      val component = new DataSourceHealthComponent("db", config, statistics)

      component.getReport.getValue must be ("1 in use of 20 (max 100)")
    }

    it("shows ok when few connections are in use") {
      val config = mock[BoneCPConfig]
      val statistics = mock[Statistics]

      given(config.getMaxConnectionsPerPartition).willReturn(50)
      given(config.getPartitionCount).willReturn(1)
      given(statistics.getTotalLeased).willReturn(1)

      val component = new DataSourceHealthComponent("db", config, statistics)

      component.getReport.getStatus must be (Status.OK)
    }

    it("shows critical when less that 10 connections are available") {
      val config = mock[BoneCPConfig]
      val statistics = mock[Statistics]

      given(config.getMaxConnectionsPerPartition).willReturn(50)
      given(config.getPartitionCount).willReturn(1)
      given(statistics.getTotalLeased).willReturn(41)

      val component = new DataSourceHealthComponent("db", config, statistics)

      component.getReport.getStatus must be (Status.CRITICAL)
    }

    it("shows warning when less that 20 connections are available") {
      val config = mock[BoneCPConfig]
      val statistics = mock[Statistics]

      given(config.getMaxConnectionsPerPartition).willReturn(50)
      given(config.getPartitionCount).willReturn(1)
      given(statistics.getTotalLeased).willReturn(31)

      val component = new DataSourceHealthComponent("db", config, statistics)

      component.getReport.getStatus must be (Status.WARNING)
    }
  }
}
