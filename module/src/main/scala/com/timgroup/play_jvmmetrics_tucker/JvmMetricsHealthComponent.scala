package com.timgroup.play_jvmmetrics_tucker

import com.timgroup.tucker.info.{Status, Component, Report}

class JvmMetricsHealthComponent extends Component("JvmMetrics", "JVMMetrics-Graphite") {
  override def getReport: Report = {
    val enabled = JvmMetrics.reporterStatus

    if (enabled) {
      new Report(Status.OK, "JvmMetrics Graphite Reporting is enabled")
    } else if (!JvmMetrics.configurationFailed) {
      new Report(Status.OK, "JvmMetrics Graphite Reporting is disabled")
    } else {
      new Report(Status.WARNING, "JvmMetrics Graphite Reporting should be enabled, but Graphite configuration failed")
    }


  }
}
