package com.timgroup.play_jvmmetrics_tucker

import com.codahale.metrics.{MetricFilter, MetricRegistry}

object TuckerMetricRegistry {
  lazy val metricRegistry = new MetricRegistry
  def clearMetricRegistry(): Unit = metricRegistry.removeMatching(MetricFilter.ALL)
}
