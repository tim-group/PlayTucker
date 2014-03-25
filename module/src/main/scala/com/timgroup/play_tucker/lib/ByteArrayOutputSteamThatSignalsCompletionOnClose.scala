package com.timgroup.play_tucker.lib

import java.io.ByteArrayOutputStream
import scala.concurrent.Promise

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
