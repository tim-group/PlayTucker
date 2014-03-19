// Comment to get more information during initialization
logLevel := Level.Warn

// The Typesafe repository
resolvers := Seq(
  "Maven Central (proxy)" at "http://repo-1/nexus/content/repositories/central/",
  "Typesafe (proxy)" at "http://repo-1/nexus/content/repositories/typesafe-releases/",
  "TIM Group Repo" at "http://repo-1/nexus/content/groups/public"
)

// Use the Play sbt plugin for Play projects
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.2.1")

// Visualize all dependencies of this project
addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.7.4")
