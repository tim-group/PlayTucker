package com.timgroup.play_jvmmetrics_tucker

import com.codahale.metrics.jvm.{ThreadStatesGaugeSet, MemoryUsageGaugeSet, GarbageCollectorMetricSet}
import com.codahale.metrics.{MetricRegistry, jvm, JvmAttributeGaugeSet}
import com.timgroup.play_metrics_graphite.Metrics
import play.api.{Logger, Application, Plugin}
import com.timgroup.play_tucker.PlayTuckerPlugin


class PlayJvmMetricsTuckerPlugin(app: Application) extends Plugin {

  override def onStart() {
    val tucker = play.api.Play.current.plugin[PlayTuckerPlugin].get
    tucker.addComponent(new JvmMetricsHealthComponent())

    JvmMetrics.start(app.configuration)
    JvmMetrics.registry.foreach(registerMetrics)

    Logger.info("PlayJvmMetricsTuckerPlugin started")
  }

  override def onStop() {
    JvmMetrics.stop()
    Logger.info("PlayJvmMetricsTuckerPlugin stopped")
  }

  private def registerMetrics(registry: MetricRegistry) {
    registry.register("jvm", new JvmAttributeGaugeSet())
    registry.register("jvm.fd_usage", new jvm.FileDescriptorRatioGauge())
    registry.register("jvm.gc", new GarbageCollectorMetricSet)
    registry.register("jvm.memory", new MemoryUsageGaugeSet())
    registry.register("jvm.thread-states", new ThreadStatesGaugeSet)
  }
}

object JvmMetrics extends Metrics
