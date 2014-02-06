package controllers

import java.io.ByteArrayOutputStream
import play.api.libs.concurrent.{Akka, Promise, akkaToPlay}
import play.api.mvc._
import play.api.mvc.Results._
import play.api.http.HeaderNames
import com.timgroup.tucker.info._
import com.timgroup.tucker.info.status.StatusPageGenerator
import akka.dispatch.ExecutionContext
import play.api.mvc.Results.Status
import akka.actor.ActorSystem
import play.api.Play
import com.timgroup.tucker.info.component.VersionComponent
import com.timgroup.play_bonecp_tucker.PlayBoneCpTuckerPlugin

trait Info extends Controller {
  def healthComponents(): Seq[Component]

  val statusPage = new StatusPageGenerator("play-bonecp-tucker-sample", PlayVersionComponent)
  healthComponents.foreach(statusPage.addComponent)

  val handler = new ApplicationInformationHandler(statusPage, Stoppable.ALWAYS_STOPPABLE, Health.ALWAYS_HEALTHY)

  def render(page: String) = Action {
    Async {
      implicit def actorSystem = currentActorSystem

      val response = new PlayWebResponse()
      handler.handle("/%s".format(page), response)
      response.stream.close() // called because Tucker's StatusPageWriter never calls this, though others do (bug in Tucker?)
      response.promiseOfResult.asPromise
    }
  }

  def currentActorSystem: ActorSystem = {
    Play.maybeApplication.map { app =>
      implicit val theApp = app
      Akka.system
    }.getOrElse(akka.actor.ActorSystem("TestSystem"))
  }
}

object Info extends Info {
  def healthComponents() = {
    import play.api.Play.current
    import com.typesafe.plugin.use

    val boneCp = use[PlayBoneCpTuckerPlugin]
    boneCp.components
  }
}

object PlayVersionComponent extends VersionComponent {
  val VERSION_PROPERTY = "timgroup.app.version"
  val DEFAULT_VERSION = "1.0.0-DEV"

  def getReport = new Report(com.timgroup.tucker.info.Status.INFO, System.getProperty(VERSION_PROPERTY, DEFAULT_VERSION))
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
