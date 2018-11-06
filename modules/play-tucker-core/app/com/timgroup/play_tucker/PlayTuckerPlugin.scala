package com.timgroup.play_tucker

import com.timgroup.tucker.info.Health.State
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
import com.timgroup.tucker.info.StartupTimer

class PlayTuckerPlugin(application: Application, appInfo: AppInfo) extends Plugin {
  import ExecutionContext.Implicits.global

  def this(application: Application) = {
    this(application, AppInfo)
  }

  private var health = Health.ALWAYS_HEALTHY
  var tucker: Option[(StatusPageGenerator, ApplicationInformationHandler)] = None
  private var startupTimer = new StartupTimer(health)

  def addComponent(component: Component) = {
    tucker.foreach(_._1.addComponent(component))
  }

  def setHealth(health: Health): Unit = {
    this.health = health
  }

  override def onStart() = {
    val appName = appInfo.getName()
    val statusPage = new StatusPageGenerator(appName, new PlayVersionComponent(appInfo))

    val handler = new ApplicationInformationHandler(statusPage, Stoppable.ALWAYS_STOPPABLE, new Health {
      override def get(): State = health.get()
    })
    tucker = Some((statusPage, handler))
    addComponent(new JvmVersionComponent())
    startupTimer.start()
  }

  override def onStop() = {
    tucker = None
    startupTimer.stop()
  }

  def render(page: String, maybeCallback: Option[String]) = Action.async {
    implicit def actorSystem = Akka.system(application)

    val response = new PlayWebResponse()
    tucker.map(_._2).foreach { handler =>
      maybeCallback match {
        case None => handler.handle("/%s".format(page), response)
        case Some(callback) => handler.handleJSONP("/%s".format(page), callback, response)
      }
    }
    response.ensureClosed() // called because Tucker's StatusPageWriter never calls this, though others do (bug in Tucker?)
    response.futureOfResult
  }
}

object Info extends Controller {
  def render(page: String, maybeCallback: Option[String] = None) = {
    play.api.Play.current.plugin[PlayTuckerPlugin].get.render(page, maybeCallback)
  }
}
