package com.timgroup.play_jvmmetrics_tucker

import play.api.{Logger, Application, Plugin}

class PlayJvmMetricsTuckerPlugin(application: Application) extends Plugin {
  lazy val component = new JvmMetricsHealthComponent()

  override def onStart() {
    import com.timgroup.play_tucker.PlayTuckerPlugin

    JvmMetrics.start()

    Logger.info("PlayJvmMetricsTuckerPlugin started")
    val tucker = play.api.Play.current.plugin[PlayTuckerPlugin].get

    tucker.addComponent(component)
  }

  override def onStop() {
    JvmMetrics.reset()

    Logger.info("PlayJvmMetricsTuckerPlugin stopped")
  }
}
