import sbt._
import sbt.Keys._

object ApplicationBuild extends Build {
  val appName = "play-tucker-sample"
  val appVersion = "1.0-SNAPSHOT"

  val appDependencies = Seq()

  lazy val compileOptions = scalacOptions ++= Seq(
    "-deprecation",
    "-Ylog-classpath",
    "-unchecked",
    "-Xfatal-warnings",
    "-Xlint")

  // Depend on the latest local code of the play module we're testing
  val module = RootProject(file("../module"))

  val main = play.Project(appName, appVersion, appDependencies)
    .settings(net.virtualvoid.sbt.graph.Plugin.graphSettings: _*)
    .settings(compileOptions)
    .dependsOn(module)
}





