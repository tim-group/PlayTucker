package com.timgroup.play_akka_tucker

import play.api.{Logger, Application, Plugin}
import com.timgroup.play_tucker.PlayTuckerPlugin

class PlayAkkaTuckerPlugin(application: Application) extends Plugin {

  override def onStart() {
    import play.api.Play.current
    import com.typesafe.plugin.use

    val tucker = use[PlayTuckerPlugin]

    components.foreach(tucker.addComponent)

    Logger.info("PlayAkkaTuckerPlugin started")
  }

  override def onStop() {
    Logger.info("PlayAkkaTuckerPlugin stopped")
  }

  def components = {
    Nil
  }
}
