package com.timgroup.play_tucker

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

  /**
   * Extends ByteArrayOutputStream to provide a Future, allowing integration
   * into Play's async response handling.
   */
  class ByteArrayOutputSteamThatSignalsCompletionOnClose extends ByteArrayOutputStream {
    private val promiseOfClosedStream = Promise[ByteArrayOutputStream]()

    def futureOfClosedStream = promiseOfClosedStream.future

    /** Signal any holders of futureOfClosedStream that writers are done */
    override def close(): Unit = {
      // NOTE (2013-10-24, msiegel): made idempotent because in Tucker, some writers close
      // and others don't, so we had to add our own additional close in PlayWebResponse above.
      if (!futureOfClosedStream.isCompleted) promiseOfClosedStream.success(this)
    }
  }
}
