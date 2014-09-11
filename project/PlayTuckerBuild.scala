import sbt._
import sbt.Keys._
import play._
import play.PlayScala
import play.PlayImport._
import play.Play.autoImport._
import play.PlayImport.PlayKeys._

object PlayTuckerBuild extends Build {
  val playVersion = "2.3.1"
  val tuckerVersion = "1.0.318"
  val metricsVersion = "3.0.2"

  val appName = "play-tucker"
  val appVersion = "1.0-SNAPSHOT"

  lazy val compileOptions = scalacOptions ++= Seq("-deprecation", "-Ylog-classpath", "-unchecked", "-Xfatal-warnings", "-Xlint")

  lazy val commonLibs = Seq(
    "com.typesafe.play" %% "play"         % playVersion,
    "com.timgroup"      %  "Tucker"       % tuckerVersion intransitive(),
    "org.slf4j"         %  "slf4j-api"    % "[1.7.6]",
    "org.mockito"       %  "mockito-core" % "1.9.0" % "test",
    "org.scalactic"     %% "scalactic"    % "2.2.0",
    "org.scalatest"     %% "scalatest"    % "2.2.0" % "test"
  )

  lazy val metrics = Seq("nl.grons"             %% "metrics-scala"    % "3.2.1",
                         "com.codahale.metrics" %  "metrics-core"     % metricsVersion,
                         "com.codahale.metrics" %  "metrics-graphite" % metricsVersion,
                         "com.codahale.metrics" %  "metrics-jvm"      % metricsVersion,
                         "com.codahale.metrics" %  "metrics-servlet"  % metricsVersion,
                         "com.codahale.metrics" %  "metrics-servlets" % metricsVersion)

  val playTuckerCore = (project in file("modules/play-tucker-core/")).enablePlugins(PlayScala)
    .settings(compileOptions)
    .settings(libraryDependencies ++= commonLibs)

  val playTuckerBoneCp = (project in file("modules/play-tucker-bonecp")).enablePlugins(PlayScala)
    .settings(compileOptions)
    .settings(libraryDependencies ++= commonLibs ++ metrics
                                    :+ jdbc
                                    :+ "mysql"              %  "mysql-connector-java" % "5.1.27"
                                    :+ "com.typesafe.play"  %% "play-jdbc"            % "2.2.1"
             )
    .dependsOn(playTuckerCore)

  val playTuckerJvmMetrics = (project in file("modules/play-tucker-jvmmetrics")).enablePlugins(PlayScala)
    .settings(compileOptions)
    .settings(libraryDependencies ++= commonLibs ++ metrics)
    .dependsOn(playTuckerCore)

  val playTuckerSampleApp = (project in file(".")).enablePlugins(PlayScala)
    .settings(compileOptions)
    .dependsOn(playTuckerCore, playTuckerBoneCp, playTuckerJvmMetrics)
    .aggregate(playTuckerCore, playTuckerBoneCp, playTuckerJvmMetrics)
}
