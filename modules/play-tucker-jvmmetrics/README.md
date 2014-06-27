Play JvmMetrics Tucker
==================

Status page components for JvmMetrics in Play 2.2.x Applications that use the PlayTucker plugin.

Usage
----------

Add the dependency in project/Build.scala

    "com.timgroup" %% "play-jvmmetrics-trucker" % "2.2.x",

Add the plugin in conf/play.plugins

    1000:com.timgroup.play_jvmmetrics_tucker.PlayJvmMetricsTuckerPlugin

This plugin automatically registers a status component with the PlayTuckerPlugin on startup and starts invokes JvmMetrics.start()