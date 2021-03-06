// Comment to get more information during initialization
logLevel := Level.Warn

// The Typesafe repository
resolvers := Seq(
  "Maven Central (proxy)" at "http://repo/nexus/content/repositories/central/",
  "Typesafe (proxy)" at "http://repo/nexus/content/repositories/typesafe-releases/",
  "TIM Group Repo" at "http://repo/nexus/content/groups/public"
)

// Visualize all dependencies of this project
addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.7.4")
