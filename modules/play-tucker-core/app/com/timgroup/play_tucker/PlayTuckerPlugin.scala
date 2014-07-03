package com.timgroup.play_tucker

import com.timgroup.tucker.info.status.StatusPageGenerator
import com.timgroup.tucker.info.ApplicationInformationHandler
import com.timgroup.tucker.info.Component
import com.timgroup.tucker.info.Stoppable
import com.timgroup.tucker.info.Health
import com.timgroup.tucker.info.component.JvmVersionComponent
import play.api.mvc.{Action, Controller}
import play.api.{Application, Plugin}
import play.api.libs.concurrent.Akka
import scala.concurrent.ExecutionContext
import com.timgroup.play_tucker.lib.PlayWebResponse
import com.timgroup.play_tucker.components.PlayVersionComponent

class PlayTuckerPlugin(application: Application, appInfo: AppInfo) extends Plugin {
  import ExecutionContext.Implicits.global

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
    tucker = Some((statusPage, handler))
    addComponent(new JvmVersionComponent())

  }

  override def onStop() = {
    tucker = None
  }

  def render(page: String) = Action.async {
    implicit def actorSystem = Akka.system(application)

    val response = new PlayWebResponse()
    tucker.foreach(_._2.handle("/%s".format(page), response))
    response.ensureClosed() // called because Tucker's StatusPageWriter never calls this, though others do (bug in Tucker?)
    response.futureOfResult
  }
}

object Info extends Controller {
  def render(page: String) = play.api.Play.current.plugin[PlayTuckerPlugin].get.render(page)
}
