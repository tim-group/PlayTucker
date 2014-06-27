name := "play-tucker"

organization := "com.timgroup"

version := "2.3." + Option(System.getProperty("BUILD_NUMBER", null)).getOrElse("0-SNAPSHOT")

scalaVersion := "2.10.4"

playVersion := "2.3.1"

tuckerVersion := "1.0.285"

resolvers += "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/"

resolvers += "repo-public" at "http://repo.youdevise.com:8081/nexus/content/groups/public"

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play" % playVersion.value,
  "com.timgroup" % "Tucker" % tuckerVersion.value intransitive(),
  "org.slf4j" % "slf4j-api" % "[1.7.6]"
)
