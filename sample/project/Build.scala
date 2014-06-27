import sbt._
import sbt.Keys._
import play._
import play.PlayScala
import play.PlayImport._
import play.Play.autoImport._
import play.PlayImport.PlayKeys._


object ApplicationBuild extends Build {
  val appName = "play-tucker-sample"
  val appVersion = "1.0-SNAPSHOT"

  lazy val compileOptions = scalacOptions ++= Seq(
    "-deprecation",
    "-Ylog-classpath",
    "-unchecked",
    "-Xfatal-warnings",
    "-Xlint")

  // Depend on the latest local code of the play module we're testing
  val module = RootProject(file("../module"))

  val main = (project in file(".")).enablePlugins(PlayScala)
    .settings(compileOptions)
    .dependsOn(module)
}





