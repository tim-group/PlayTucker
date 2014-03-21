Play Tucker
===========

Tucker for Play applications



Installation (for Play 2.1)

-- For Play 2.2.x use the play22x branch

------------

Add the dependency in project/Build.scala

    "com.timgroup" %% "play-tucker" % "x.y.z",

Add plugin to conf/play.plugins

    250:com.timgroup.play_tucker.PlayTuckerPlugin

Add a route to conf/routes

    GET        /info/:page        com.timgroup.play_tucker.Info.render(page)
