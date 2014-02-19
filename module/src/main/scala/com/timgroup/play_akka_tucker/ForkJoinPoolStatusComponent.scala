package com.timgroup.play_akka_tucker

import com.timgroup.tucker.info.{Report, Component}
import com.timgroup.tucker.info.Status
import play.api.libs.concurrent.Akka
import akka.dispatch.Dispatcher
import akka.jsr166y.ForkJoinPool

class ForkJoinPoolStatusComponent(val executionContextName: String, val forkJoinPool: ForkJoinPool)
  extends Component("Akka-" + executionContextName, "Execution Context %s Thread Pool Status".format(executionContextName)) {

  override def getReport: Report = {
    new Report(Status.INFO, forkJoinPool.toString.split('[').last.stripSuffix("]"))
  }

}
