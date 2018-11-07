import play.PlayImport.PlayKeys._

scalaVersion in ThisBuild := "2.11.12"

organization in ThisBuild := "com.timgroup"

version in ThisBuild := "2.3." + Option(System.getProperty("BUILD_NUMBER", null)).getOrElse("0-SNAPSHOT")

resolvers += "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/"

resolvers += "repo-public" at "http://repo.youdevise.com:8081/nexus/content/groups/public"

publishTo in ThisBuild := Some("TIM Group Repo" at "http://repo.youdevise.com:8081/nexus/content/repositories/yd-release-candidates")

credentials += Credentials(new File("/etc/sbt/credentials"))


crossScalaVersions := Seq("2.11.5")

val playVersion = play.core.PlayVersion.current // see /project/play.sbt
val tuckerVersion = "1.0.1511"
val metricsVersion = "4.0.3"

val appName = "play-tucker"
val appVersion = "1.0-SNAPSHOT"

lazy val compileOptions = scalacOptions ++= Seq("-deprecation", "-Ylog-classpath", "-unchecked", "-Xfatal-warnings", "-Xlint:_,-missing-interpolator", "-feature")

lazy val commonLibs = Seq(
  "com.typesafe.play" %% "play"         % playVersion,
  "com.timgroup"      %  "Tucker"       % tuckerVersion intransitive(),
  "org.slf4j"         %  "slf4j-api"    % "[1.7.6]",
  "org.mockito"       %  "mockito-core" % "1.10.19" % "test",
  "org.scalactic"     %% "scalactic"    % "3.0.5",
  "org.scalatest"     %% "scalatest"    % "3.0.5" % "test"
)

val playTuckerCore = (project in file("modules/play-tucker-core/")).enablePlugins(PlayScala)
  .settings(compileOptions)
  .settings(libraryDependencies ++= commonLibs)

val playMetricsGraphite = (project in file("modules/play-metrics-graphite/")).enablePlugins(PlayScala)
  .settings(compileOptions)
  .settings(libraryDependencies ++= commonLibs
                                  :+ "io.dropwizard.metrics" %  "metrics-core"     % metricsVersion
                                  :+ "io.dropwizard.metrics" %  "metrics-graphite" % metricsVersion
                                  :+ "io.dropwizard.metrics" %  "metrics-jvm"      % metricsVersion
                                  :+ "io.dropwizard.metrics" %  "metrics-servlet"  % metricsVersion
                                  :+ "io.dropwizard.metrics" %  "metrics-servlets" % metricsVersion)

val playTuckerBoneCp = (project in file("modules/play-tucker-bonecp")).enablePlugins(PlayScala)
  .settings(compileOptions)
  .settings(libraryDependencies ++= commonLibs
                                  :+ jdbc
                                  :+ "mysql"              %  "mysql-connector-java" % "8.0.13"
           )
  .dependsOn(playTuckerCore)
  .dependsOn(playMetricsGraphite)

val playTuckerJvmMetrics = (project in file("modules/play-tucker-jvmmetrics")).enablePlugins(PlayScala)
  .settings(compileOptions)
  .settings(libraryDependencies ++= commonLibs)
  .dependsOn(playTuckerCore)
  .dependsOn(playMetricsGraphite)

val playTuckerSampleApp = (project in file(".")).enablePlugins(PlayScala)
  .settings(compileOptions)
  .dependsOn(playTuckerCore, playMetricsGraphite, playTuckerBoneCp, playTuckerJvmMetrics)
  .aggregate(playTuckerCore, playMetricsGraphite, playTuckerBoneCp, playTuckerJvmMetrics)
  .settings(routesImport ++= Seq("scala.language.reflectiveCalls"))
