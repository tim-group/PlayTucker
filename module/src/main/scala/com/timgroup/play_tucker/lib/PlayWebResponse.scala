package com.timgroup.play_tucker.lib

import com.timgroup.tucker.info.WebResponse
import play.api.mvc.Results._
import scala.concurrent.{Promise, Future, ExecutionContext}
import play.api.http.HeaderNames
import java.io.ByteArrayOutputStream
import play.api.mvc.SimpleResult

class PlayWebResponse(implicit ec: ExecutionContext) extends WebResponse {
  private val stream = new ByteArrayOutputSteamThatSignalsCompletionOnClose()
  private val promiseOfResult = Promise[SimpleResult]()
  val futureOfResult: Future[SimpleResult] = promiseOfResult.future

  def ensureClosed(): Unit = {
    stream.close()
  }

  def reject(status: Int, message: String) { promiseOfResult.success(Status(status)(message)) }

  def redirect(relativePath: String) { promiseOfResult.success(Redirect(relativePath)) }

  def respond(contentType: String, characterEncoding: String) = {
    promiseOfResult.completeWith(stream.futureOfClosedStream.map {
      closedStream =>
        Ok(closedStream.toString(characterEncoding)).withHeaders(HeaderNames.CONTENT_TYPE -> "%s; charset=%s".format(contentType, characterEncoding))
    })

    stream
  }
}
