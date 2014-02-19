package com.timgroup.play_tucker

import play.api.Plugin
import play.api.Application
import com.timgroup.tucker.info.status.StatusPageGenerator
import com.timgroup.tucker.info.ApplicationInformationHandler
import com.timgroup.tucker.info.Component
import com.timgroup.tucker.info.Stoppable
import com.timgroup.tucker.info.Health
import com.timgroup.tucker.info.Report
import play.api.mvc.{Controller, Action}
import com.timgroup.tucker.info.component.{JvmVersionComponent, VersionComponent}
import play.api.mvc.Results._
import play.api.libs.concurrent.Akka
import play.api.libs.concurrent.akkaToPlay

class PlayVersionComponent(appInfo: AppInfo) extends VersionComponent {
  def getReport = new Report(com.timgroup.tucker.info.Status.INFO, appInfo.getVersion())
}

class PlayTuckerPlugin(application: Application, appInfo: AppInfo) extends Plugin {

  def this(application: Application) = {
    this(application, AppInfo)
  }

  var tucker: Option[(StatusPageGenerator, ApplicationInformationHandler)] = None

  def addComponent(component: Component) = {
    tucker.foreach(_._1.addComponent(component))
  }

  override def onStart() = {
    val appName = appInfo.getName()
    val statusPage = new StatusPageGenerator(appName, new PlayVersionComponent(appInfo))
    val handler = new ApplicationInformationHandler(statusPage, Stoppable.ALWAYS_STOPPABLE, Health.ALWAYS_HEALTHY)
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
    import play.api.Play.current
    import com.typesafe.plugin.use
    val playTucker = use[PlayTuckerPlugin]
    playTucker.render(page)
  }
}
