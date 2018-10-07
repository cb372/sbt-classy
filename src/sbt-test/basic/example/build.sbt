scalaVersion := "2.12.7"
enablePlugins(ClassyPlugin)

val monocleVersion = "1.5.1-cats"
libraryDependencies ++= Seq(
  "com.github.julien-truffaut" %% "monocle-core" % monocleVersion,
  "com.github.julien-truffaut" %% "monocle-macro" % monocleVersion
)
