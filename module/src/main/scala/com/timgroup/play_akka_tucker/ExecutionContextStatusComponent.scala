package com.timgroup.play_akka_tucker

import com.timgroup.tucker.info.{Report, Component}
import com.timgroup.tucker.info.Status

class ExecutionContextStatusComponent(val executionContextName: String)
  extends Component("Akka-" + executionContextName, "Execution Context %s Thread Pool Status".format(executionContextName)) {

  override def getReport: Report = {
    new Report(Status.INFO, "is there")
  }

}
