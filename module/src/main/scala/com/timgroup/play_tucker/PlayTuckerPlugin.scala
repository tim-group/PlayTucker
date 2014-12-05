package com.timgroup.play_tucker

import com.timgroup.tucker.info.Health.State
import com.timgroup.tucker.info.{ApplicationInformationHandler, Component, Health, Report, Stoppable}
import com.timgroup.tucker.info.component.{JvmVersionComponent, VersionComponent}
import com.timgroup.tucker.info.status.StatusPageGenerator
import play.api.{Application, Plugin}
import play.api.libs.concurrent.{Akka, akkaToPlay}
import play.api.mvc.Results._
import play.api.mvc.{Action, Controller}

class PlayVersionComponent(appInfo: AppInfo) extends VersionComponent {
  def getReport = new Report(com.timgroup.tucker.info.Status.INFO, appInfo.getVersion())
}

class PlayTuckerPlugin(application: Application, appInfo: AppInfo) extends Plugin {

  def this(application: Application) = {
    this(application, AppInfo)
  }

  var tucker: Option[(StatusPageGenerator, ApplicationInformationHandler)] = None

  var health: Health = Health.ALWAYS_HEALTHY

  def addComponent(component: Component) = {
    tucker.foreach(_._1.addComponent(component))
  }

  def setHealth(health: Health) {
    this.health = health
  }

  override def onStart() = {
    val appName = appInfo.getName()
    val statusPage = new StatusPageGenerator(appName, new PlayVersionComponent(appInfo))
    val handler = new ApplicationInformationHandler(statusPage, Stoppable.ALWAYS_STOPPABLE, new Health {
      override def get(): State = health.get()
    })

    tucker = Some(statusPage, handler)
    addComponent(new JvmVersionComponent())
  }

  override def onStop() = {
    tucker = None
  }

  def render(page: String) = Action {
    Async {
      implicit def actorSystem = Akka.system(application)

      val response = new PlayWebResponse()
      tucker.foreach(_._2.handle("/%s".format(page), response))
      response.stream.close() // called because Tucker's StatusPageWriter never calls this, though others do (bug in Tucker?)
      response.promiseOfResult.asPromise
    }
  }
}

object Info extends Controller {
  def render(page: String) = {
    import com.typesafe.plugin.use
    import play.api.Play.current
    val playTucker = use[PlayTuckerPlugin]
    playTucker.render(page)
  }
}
