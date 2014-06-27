import sbt._
import Keys._

object PlayTuckerBuild extends Build {
  val playVersion = SettingKey[String]("play-version", "The version of Play Framework used for building.")
  val tuckerVersion = SettingKey[String]("tucker-version", "The version of Tucker used for building.")

  lazy val main = Project(
    id        = "play-tucker",
    base      = file( "." )
  ).settings(publishTo := Some("TIM Group Repo" at "http://repo.youdevise.com:8081/nexus/content/repositories/yd-release-candidates"))
   .settings(credentials += Credentials(new File("/etc/sbt/credentials")))

}
