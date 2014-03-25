package com.timgroup.play_tucker.components

import com.timgroup.play_tucker.AppInfo
import com.timgroup.tucker.info.component.VersionComponent
import com.timgroup.tucker.info.Report

class PlayVersionComponent(appInfo: AppInfo) extends VersionComponent {
  def getReport = new Report(com.timgroup.tucker.info.Status.INFO, appInfo.getVersion())
}
