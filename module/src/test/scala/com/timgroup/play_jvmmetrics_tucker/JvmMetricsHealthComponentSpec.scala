package com.timgroup.play_jvmmetrics_tucker

import org.scalatest._
import matchers.MustMatchers
import com.timgroup.tucker.info.Status

class JvmMetricsHealthComponentSpec extends path.FunSpec with MustMatchers with OneInstancePerTest {
  describe("The JvmMetrics health component:") {

    def startAndGetHealthComponent() = {
      JvmMetrics.start()
      new JvmMetricsHealthComponent()
    }

    describe("shows OK when enabled and all required properties are set") {
      System.setProperty("graphite.enabled", "true")
      System.setProperty("graphite.host", "127.0.0.1")
      System.setProperty("graphite.port", "12345")
      System.setProperty("graphite.prefix", "somePrefix")

      val component = startAndGetHealthComponent()

      component.getReport.getStatus must be(Status.OK)
    }

    it("shows OK when the GraphiteReporter is not enabled") {
      System.setProperty("graphite.enabled", "false")

      val component = startAndGetHealthComponent()

      component.getReport.getStatus must be(Status.OK)
    }

    it("shows WARNING when the GraphiteReporter is enabled, but configuration is invalid") {
      System.setProperty("graphite.enabled", "true")
      System.setProperty("graphite.port", "You shall not parse!")

      val component = startAndGetHealthComponent()

      component.getReport.getStatus must be(Status.WARNING)
    }
  }
}
