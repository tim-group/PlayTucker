package com.timgroup.play_bonecp_tucker

import com.timgroup.tucker.info.{Status, Report, Component}
import com.typesafe.plugin._
import com.jolbox.bonecp.BoneCPDataSource

object PlayBoneCpTuckerPlugin {
  def components = {
    import play.api.Play.current
    import com.typesafe.plugin.use
    import play.api.db.BoneCPPlugin

    val boneCp = use[BoneCPPlugin]

    boneCp.api.datasources.map {
      case (datasource: BoneCPDataSource, datasourceName: String) =>  {
        new Component("BoneCp-" + datasourceName, "BoneCp-" + datasourceName) {
          override def getReport: Report = new Report(Status.OK)
        }
      }
    }
  }
}
