package com.timgroup.play_jvmmetrics_tucker

import com.timgroup.tucker.info.{Status, Component, Report}

class JvmMetricsHealthComponent extends Component("JvmMetrics", "JVMMetrics-Graphite") {
  override def getReport: Report = {
    if (JvmMetrics.configurationFailed) {
      new Report(Status.WARNING, "JvmMetrics Graphite Reporting should be enabled, but Graphite configuration failed")
    } else if (JvmMetrics.registry.isDefined) {
      new Report(Status.OK, "JvmMetrics Graphite Reporting is enabled")
    } else {
      new Report(Status.OK, "JvmMetrics Graphite Reporting is disabled")
    }
  }
}
