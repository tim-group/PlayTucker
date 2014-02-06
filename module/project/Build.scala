import sbt._
import Keys._

object PlayBoneCpTuckerBuild extends Build {
  val playVersion = SettingKey[String]("play-version", "The version of Play Framework used for building.")

  val libs = Seq("com.timgroup"       %  "Tucker"         % "1.0.272" intransitive(),
                 "org.slf4j"          %  "slf4j-api"      % "[1.7.6]",
                 "com.yammer.metrics" %  "metrics-core"   % "2.0.2",
                 "org.mockito"        %  "mockito-core"   % "1.9.0" % "test",
                 "org.scalatest"      %  "scalatest_2.10" % "1.9.2" % "test",
                 "org.specs2"         %  "specs2_2.10"    % "1.14"  % "test")

  lazy val main = Project(id = "play-bonecp-tucker", base = file( "." ))
   .settings(libraryDependencies ++= libs)
   .settings(publishTo := Some("TIM Group Repo" at "http://repo.youdevise.com:8081/nexus/content/repositories/yd-release-candidates"))
   .settings(credentials += Credentials(new File("/etc/sbt/credentials")))     
}
