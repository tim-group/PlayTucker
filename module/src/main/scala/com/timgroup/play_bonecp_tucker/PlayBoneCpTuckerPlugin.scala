package com.timgroup.play_bonecp_tucker

import com.timgroup.tucker.info.{Status, Report, Component}

object PlayBoneCpTuckerPlugin {
  def components = Seq(
    new Component("BoneCp-default", "BoneCp-default") {
      override def getReport: Report = new Report(Status.OK)
    }
  )
}
