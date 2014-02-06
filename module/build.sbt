name := "play-bonecp-tucker"

organization := "com.timgroup"

version := "0.0." + Option(System.getProperty("BUILD_NUMBER", null)).getOrElse("0-SNAPSHOT")

scalaVersion := "2.10.3"

crossScalaVersions := Seq("2.10.3")

playVersion := "2.2.1"

resolvers += "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/"

resolvers += "repo-public" at "http://repo.youdevise.com:8081/nexus/content/groups/public"
