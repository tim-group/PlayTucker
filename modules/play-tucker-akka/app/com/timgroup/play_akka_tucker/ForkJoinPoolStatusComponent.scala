package com.timgroup.play_akka_tucker

import akka.dispatch.Dispatcher
import akka.jsr166y.ForkJoinPool
import akka.util.NonFatal
import play.api.libs.concurrent.Akka
import play.Logger
import com.timgroup.tucker.info.{Component, Report, Status}
import com.yammer.metrics.core.{Gauge, MetricName}
import com.yammer.metrics.Metrics

class ForkJoinPoolStatusComponent(val executionContextName: String, val forkJoinPool: ForkJoinPool)
  extends Component("Akka-" + executionContextName, "Execution Context %s Thread Pool Status".format(executionContextName)) {

  try {
    implicit val metricPrefix = new MetricName("akka-forkjoinpool", executionContextName, "")
    gauge("ActiveThreadCount", () => forkJoinPool.getActiveThreadCount)
    gauge("Parallelism", () => forkJoinPool.getParallelism)
    gauge("PoolSize", () => forkJoinPool.getPoolSize)
    gauge("QueuedSubmissionCount", () => forkJoinPool.getQueuedSubmissionCount)
    gauge("QueuedTaskCount", () => forkJoinPool.getQueuedTaskCount)
    gauge("RunningThreadCount", () => forkJoinPool.getRunningThreadCount)
    gauge("StealCount", () => forkJoinPool.getStealCount)
  } catch {
    case NonFatal(e) => Logger.error("Error registering metrics for Akka ForkJoinPool " + executionContextName, e)
  }

  private def gauge[T <: Any](name: String, f: () => T)(implicit top: MetricName) = {
    Metrics.defaultRegistry().newGauge(new MetricName(top.getGroup, top.getType, name), new Gauge[T] {
      def value() = f()
    })
  }

  override def getReport: Report = {
    new Report(Status.INFO, forkJoinPool.toString.split('[').last.stripSuffix("]"))
  }

}
