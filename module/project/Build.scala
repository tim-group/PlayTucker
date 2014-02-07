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

  // NOTE (2013-11-14, Marc): Sometimes Scala libraries (such as
  //   ScalaTest and Specs2) will have a latest published version
  //   for older Scala versions, and earliest version for newer Scala
  //   versions. Also, they may publish a single artifact across
  //   binary-compatible scala versions such as 2.10.x, which defeats
  //   the purpose of sbt's '%%' operator.
  object CrossVersionDependencies {
    val scala_2_9_0 = """2\.9\.0.*""".r
    val scala_2_9   = """2\.9.*""".r
    val scala_2_10  = """2\.10.*""".r

    def scalatest(scalaVersion: String) = scalaVersion match {
      case scala_2_9()  => Seq("org.scalatest" %% "scalatest" % "1.9.2" % "test")
      case scala_2_10() => Seq("org.scalatest" % "scalatest_2.10" % "1.9.2" % "test")
    }

    def specs2(scalaVersion: String) = scalaVersion match {
      case scala_2_9_0() => Seq("org.specs2" %% "specs2" % "1.7.1" % "test")
      case scala_2_9()   => Seq("org.specs2" %% "specs2" % "1.12.3" % "test")
      case scala_2_10    => Seq("org.specs2" % "specs2_2.10" % "1.14" % "test")
    }

    def play(scalaVersion: String, playVersion: String) = Seq("play" % ("play_" + scalaVersion) % playVersion)
  }

}
