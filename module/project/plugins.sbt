resolvers := Seq(
  "Maven Central (proxy)" at "http://repo-1/nexus/content/repositories/central/",
  "Typesafe (proxy)" at "http://repo-1/nexus/content/repositories/typesafe-releases/",
  "TIM Group Repo" at "http://repo-1/nexus/content/groups/public"
)

// Generate project for IntelliJ with sbt-idea
addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.1.0")
