package com.timgroup.play_bonecp_tucker

import java.net.InetSocketAddress
import java.util.concurrent.TimeUnit

import com.codahale.metrics.graphite.{Graphite, GraphiteReporter}
import com.codahale.metrics.{MetricFilter, MetricRegistry}
import play.api.{Configuration, Application, Logger}

import scala.util.{Failure, Success, Try}

class Metrics {
  var registry: Option[MetricRegistry] = None
  var graphiteReporter: Option[GraphiteReporter] = None

  def start(playConfig: Configuration) {
    val graphiteEnabled = playConfig.getBoolean("graphite.enabled").getOrElse(false)

    if (graphiteEnabled) {
      val tryGraphiteConfig = Try[GraphiteConfiguration] {
        val host = playConfig.getString("graphite.host").get
        val port = playConfig.getInt("graphite.port").get
        val prefix = playConfig.getString("graphite.prefix").get
        GraphiteConfiguration(host, port, prefix)
      }

      tryGraphiteConfig match {
        case Success(graphiteConfig) =>
          registry = Some(new MetricRegistry)
          val graphite = new Graphite(new InetSocketAddress(graphiteConfig.host, graphiteConfig.port))
          graphiteReporter = Some(
            GraphiteReporter.forRegistry(registry.get).prefixedWith(graphiteConfig.prefix).build(graphite)
          )
          graphiteReporter.foreach(_.start(10, TimeUnit.SECONDS))

        case Failure(e) =>
          Logger.error("Graphite reporting is enabled but the configuration failed with error: " + e.toString, e)
      }
    }
  }

  def stop() {
    graphiteReporter.foreach(_.stop())
    registry.foreach(_.removeMatching(MetricFilter.ALL))
    graphiteReporter = None
    registry = None
  }
}

case class GraphiteConfiguration(host: String, port: Int, prefix: String)
