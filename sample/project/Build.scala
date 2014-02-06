import sbt._
import PlayProject._

object ApplicationBuild extends Build {
  val appName = "play-bonecp-tucker-sample"
  val appVersion = "1.0-SNAPSHOT"

  val appDependencies = Seq()

  // Depend on the latest local code of the play module we're testing
  val module = RootProject(file("../module"))

  val main = PlayProject(appName, appVersion, appDependencies)
    .settings(resolvers := Seq(
    "Maven Central (proxy)" at "http://repo-1/nexus/content/repositories/central/",
    "Typesafe (proxy)" at "http://repo-1/nexus/content/repositories/typesafe-releases/",
    "TIM Group Repo" at "http://repo-1/nexus/content/groups/public"))
    .settings(net.virtualvoid.sbt.graph.Plugin.graphSettings: _*)
    .dependsOn(module)
}
