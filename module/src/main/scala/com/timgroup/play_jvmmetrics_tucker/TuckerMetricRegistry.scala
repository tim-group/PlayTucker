package com.timgroup.play_jvmmetrics_tucker

import com.codahale.metrics.{jvm, JvmAttributeGaugeSet, MetricRegistry}

object TuckerMetricRegistry {
  val metricRegistry = new MetricRegistry

  import com.codahale.metrics.jvm._
  metricRegistry.register("jvm", new JvmAttributeGaugeSet())
  metricRegistry.register("jvm.fd_usage", new jvm.FileDescriptorRatioGauge())
  metricRegistry.register("jvm.gc", new GarbageCollectorMetricSet)
  metricRegistry.register("jvm.memory", new MemoryUsageGaugeSet())
  metricRegistry.register("jvm.thread-states", new ThreadStatesGaugeSet)
}
