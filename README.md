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
