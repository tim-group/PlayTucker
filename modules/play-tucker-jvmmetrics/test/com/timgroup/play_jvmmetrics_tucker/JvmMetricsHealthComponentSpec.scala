package com.timgroup.play_jvmmetrics_tucker

import com.timgroup.tucker.info.{Report, Status}
import com.typesafe.config.ConfigFactory
import org.scalatest._
import play.api.Configuration

class JvmMetricsHealthComponentSpec extends path.FunSpec with MustMatchers with OneInstancePerTest {
  describe("The JvmMetrics health component:") {

    it("shows OK when enabled and all required properties are set") {
      val component = startAndGetHealthComponent(Map("graphite.enabled" -> "true",
                                                     "graphite.host" -> "127.0.0.1",
                                                     "graphite.port" -> "12345",
                                                     "graphite.prefix" -> "somePrefix"))

      component.getReport must equal(new Report(Status.OK, "JvmMetrics Graphite Reporting is enabled"))
    }

    it("shows OK when the GraphiteReporter is not enabled") {
      val component = startAndGetHealthComponent(Map("graphite.enabled" -> "false"))

      component.getReport must equal(new Report(Status.OK, "JvmMetrics Graphite Reporting is disabled"))
    }

    it("shows WARNING when the GraphiteReporter is enabled, but configuration is invalid") {
      val component = startAndGetHealthComponent(Map("graphite.enabled" -> "true",
                                                     "graphite.port" -> "INVALID"))

      component.getReport must equal(new Report(Status.WARNING, "JvmMetrics Graphite Reporting should be enabled, but Graphite configuration failed"))
    }
  }

  def startAndGetHealthComponent(config: Map[String, String]) = {
    import scala.collection.JavaConversions._
    val playConfig = Configuration(ConfigFactory.parseMap(config))
    JvmMetrics.start(playConfig)
    new JvmMetricsHealthComponent()
  }
}
