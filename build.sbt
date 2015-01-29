scalaVersion in ThisBuild := "2.10.4"

organization in ThisBuild := "com.timgroup"

version in ThisBuild := "2.3." + Option(System.getProperty("BUILD_NUMBER", null)).getOrElse("0-SNAPSHOT")

resolvers += "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/"

resolvers += "repo-public" at "http://repo.youdevise.com:8081/nexus/content/groups/public"

publishTo in ThisBuild := Some("TIM Group Repo" at "http://repo.youdevise.com:8081/nexus/content/repositories/yd-release-candidates")

credentials += Credentials(new File("/etc/sbt/credentials"))


crossScalaVersions := Seq("2.10.4", "2.11.5")

