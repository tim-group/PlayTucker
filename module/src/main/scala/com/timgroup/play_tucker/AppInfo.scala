package com.timgroup.play_tucker

class AppInfo {
  val VERSION_PROPERTY = "timgroup.app.version"
  val DEFAULT_VERSION = "1.0.0-DEV"

  val NAME_PROPERTY = "timgroup.app.name"
  val DEFAULT_NAME = "local-app"

  def getVersion() = {
    System.getProperty(VERSION_PROPERTY, DEFAULT_VERSION)
  }

  def getName() = {
    System.getProperty(NAME_PROPERTY, DEFAULT_NAME)
  }
}

object AppInfo extends AppInfo
