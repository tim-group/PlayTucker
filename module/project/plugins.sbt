resolvers := Seq(
  "Maven Central (proxy)" at "http://repo-1/nexus/content/repositories/central/",
  "Typesafe (proxy)" at "http://repo-1/nexus/content/repositories/typesafe-releases/",
  "TIM Group Repo" at "http://repo-1/nexus/content/groups/public"
)

addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.5.2")

