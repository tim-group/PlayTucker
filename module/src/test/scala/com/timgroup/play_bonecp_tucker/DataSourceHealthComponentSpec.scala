package com.timgroup.play_bonecp_tucker

import org.scalatest._
import matchers.MustMatchers
import org.scalatest.mock.MockitoSugar
import com.jolbox.bonecp.{Statistics, BoneCPConfig}
import com.timgroup.tucker.info.Status
import org.mockito.BDDMockito._

class DataSourceHealthComponentSpec extends path.FunSpec with MockitoSugar with MustMatchers {
  describe("The datasource health") {
    it("shows ok when few connections are in use") {
      val config = mock[BoneCPConfig]
      val statistics = mock[Statistics]

      given(config.getMaxConnectionsPerPartition).willReturn(50)
      given(statistics.getTotalLeased).willReturn(1)

      val component = new DataSourceHealthComponent("db", config, statistics)

      component.getReport.getStatus must be (Status.OK)
    }

    it("shows critical when less that 10 connections are available") {
      val config = mock[BoneCPConfig]
      val statistics = mock[Statistics]

      given(config.getMaxConnectionsPerPartition).willReturn(50)
      given(statistics.getTotalLeased).willReturn(41)

      val component = new DataSourceHealthComponent("db", config, statistics)

      component.getReport.getStatus must be (Status.CRITICAL)
    }
  }
}
