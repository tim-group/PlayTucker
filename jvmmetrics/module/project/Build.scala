import sbt._
import Keys._

object JvmMetricsBuild extends Build {
  val playVersion = SettingKey[String]("play-version", "The version of Play Framework used for building.")

  lazy val metricsVersion = "3.0.0"
  lazy val metrics = Seq("nl.grons"             %% "metrics-scala"   % "3.0.4",
    //Java Libraries:
    "com.codahale.metrics" % "metrics-core"     % metricsVersion,
    "com.codahale.metrics" % "metrics-graphite" % metricsVersion,
    "com.codahale.metrics" % "metrics-jvm"      % metricsVersion,
    "com.codahale.metrics" % "metrics-servlet"  % metricsVersion,
    "com.codahale.metrics" % "metrics-servlets" % metricsVersion)

  val libs = Seq("com.timgroup"       %  "Tucker"         % "1.0.285" intransitive(),
                 "org.slf4j"          %  "slf4j-api"      % "[1.7.6]",
                 "com.timgroup"       %% "play-tucker"    % "2.2.3",
                 "org.scalatest"      %  "scalatest_2.10" % "2.1.0" % "test") ++ metrics

  lazy val main = Project(id = "play-jvmmetrics-tucker", base = file( "." ))
   .settings(libraryDependencies ++= libs)
   .settings(publishTo := Some("TIM Group Repo" at "http://repo.youdevise.com:8081/nexus/content/repositories/yd-release-candidates"))
   .settings(credentials += Credentials(new File("/etc/sbt/credentials")))     
}
