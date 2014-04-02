package com.timgroup.play_jvmmetrics_tucker

import java.util.concurrent.TimeUnit
import com.codahale.metrics.graphite.Graphite
import com.codahale.metrics.graphite.GraphiteReporter
import java.net.InetSocketAddress
import scala.util.{Failure, Success, Try}

object JvmMetrics {

  private var reporter: Option[GraphiteReporter] = None
  private var configurationAttempt: Option[Boolean] = None

  case class GraphiteConfiguration(host: String,port: Int,prefix: String)

  def start() {
    reset()

    val graphiteEnabled: Boolean = System.getProperty("graphite.enabled", "false").toBoolean
    if (graphiteEnabled) {
      val tryConfig = Try[GraphiteConfiguration] {
        val host: String = System.getProperty("graphite.host")
        val port: Int = System.getProperty("graphite.port").toInt
        val prefix: String = System.getProperty("graphite.prefix")

        GraphiteConfiguration(host,port,prefix)
      }

      tryConfig match {
        case Success(gc) =>
          configurationAttempt = Some(true)

          val graphite = new Graphite(new InetSocketAddress(gc.host, gc.port))
          reporter = Some(
            GraphiteReporter.forRegistry(TuckerMetricRegistry.metricRegistry).prefixedWith(gc.prefix).build(graphite)
          )
          reporter.foreach(_.start(10, TimeUnit.SECONDS))
        case Failure(e) =>
          configurationAttempt = Some(false)
          reporter = None
      }
    }
  }

  def reporterStatus: Boolean = reporter.isDefined

  def configurationAttempted = configurationAttempt.isDefined

  def configurationFailed = configurationAttempt.isDefined && !configurationAttempt.get

  def reset() {
    reporter.foreach(_.stop())
    reporter = None
    configurationAttempt = None
  }
}
