Play Tucker
===========

Tucker for Play applications

Installation
------------

Add the dependency in project/Build.scala

    "com.timgroup" %% "play-tucker" % "x.y.z",

Add plugin to conf/play.plugins

    250:com.timgroup.play_tucker.PlayTuckerPlugin

Add a route to conf/routes

    GET        /info/:page        com.timgroup.play_tucker.Info.render(page)








Play BoneCP Tucker
==================

Status page components for BoneCP DataSources in Play Applications

How to use
----------

Add the dependency in project/Build.scala

    "com.timgroup" %% "play-bonecp-tucker" % "x.y.z",

Add the plugin in conf/play.plugins

    500:com.timgroup.play_bonecp_tucker.PlayBoneCpTuckerPlugin

Add the components to Tucker

    import play.api.Play.current
    import com.typesafe.plugin.use
    val boneCP = use[PlayBoneCpTuckerPlugin]

    boneCP.components.foreach(statusPage.addComponent)


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
