package com.timgroup.play_tucker

import java.io.ByteArrayOutputStream
import play.api.Plugin
import play.api.Application
import com.timgroup.tucker.info.status.StatusPageGenerator
import com.timgroup.tucker.info.ApplicationInformationHandler
import com.timgroup.tucker.info.Component
import com.timgroup.tucker.info.Stoppable
import com.timgroup.tucker.info.Health
import com.timgroup.tucker.info.Report
import com.timgroup.tucker.info.WebResponse
import play.api.mvc.{Controller, Result, Action}

import com.timgroup.tucker.info.component.VersionComponent
import akka.dispatch.ExecutionContext
import play.api.mvc.Results._
import play.api.http.HeaderNames
import play.api.libs.concurrent.Akka
import play.api.libs.concurrent.akkaToPlay

class PlayTuckerPlugin(application: Application) extends Plugin {
  var tucker: Option[(StatusPageGenerator, ApplicationInformationHandler)] = None

  def addComponent(component: Component) = {
    tucker.foreach(_._1.addComponent(component))
  }

  override def onStart() = {
    val statusPage = new StatusPageGenerator("ideasfx", PlayVersionComponent)
    val handler = new ApplicationInformationHandler(statusPage, Stoppable.ALWAYS_STOPPABLE, Health.ALWAYS_HEALTHY)
    tucker = Some(statusPage, handler)
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

object PlayVersionComponent extends VersionComponent {
  def getReport = new Report(com.timgroup.tucker.info.Status.INFO, AppVersion.fromSystem())
}

class AppVersion {
  import AppVersion._
  def fromSystem(): String = {
    System.getProperty(VERSION_PROPERTY, DEFAULT_VERSION)
  }
}

object AppVersion extends AppVersion {
  val VERSION_PROPERTY = "timgroup.app.version"
  val DEFAULT_VERSION = "1.0.0-DEV"

  def apply() = new AppVersion()
}

class PlayWebResponse(implicit ec: ExecutionContext) extends WebResponse {
  val promiseOfResult = akka.dispatch.Promise[Result]
  val stream = new ByteArrayOutputSteamThatSignalsCompletionOnClose()

  def futureOfResult = promiseOfResult.future

  def reject(status: Int, message: String) { promiseOfResult.success(Status(status)(message)) }

  def redirect(relativePath: String) { promiseOfResult.success(Redirect(relativePath)) }

  def respond(contentType: String, characterEncoding: String) = {
    promiseOfResult.completeWith {
      stream.futureOfClosedStream.map { closedStream =>
        Ok(closedStream.toString(characterEncoding)).withHeaders(HeaderNames.CONTENT_TYPE -> "%s; charset=%s".format(contentType, characterEncoding))
      }
    }

    stream
  }

  /**
   * Extends ByteArrayOutputStream to provide a Future, allowing integration
   * into Play's async response handling.
   */
  class ByteArrayOutputSteamThatSignalsCompletionOnClose extends ByteArrayOutputStream {
    private val promiseOfClosedStream = akka.dispatch.Promise[ByteArrayOutputStream]

    def futureOfClosedStream = promiseOfClosedStream.future

    /** Signal any holders of futureOfClosedStream that writers are done */
    override def close(): Unit = {
      // NOTE (2013-10-24, msiegel): made idempotent because in Tucker, some writers close
      // and others don't, so we had to add our own additional close in PlayWebResponse above.
      if (!promiseOfClosedStream.isCompleted) promiseOfClosedStream.success(this)
    }
  }
}