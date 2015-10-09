name in ThisBuild := "play-tucker"

version in ThisBuild := "2.4." + Option(System.getProperty("BUILD_NUMBER", null)).getOrElse("0-SNAPSHOT")

organization in ThisBuild := "com.timgroup"

scalaVersion in ThisBuild := "2.11.7"

publishTo in ThisBuild := Some("TIM Group Repo" at "http://repo.youdevise.com:8081/nexus/content/repositories/yd-release-candidates")

credentials in ThisBuild += Credentials(new File("/etc/sbt/credentials"))

