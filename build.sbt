enablePlugins(SbtPlugin)

scalaVersion := "2.12.7"
organization := "com.github.cb372"

libraryDependencies ++= Seq(
  "org.scalameta" %% "scalameta" % "4.0.0",
  "org.scalameta" %% "contrib" % "4.0.0",
  "org.scalatest" %% "scalatest" % "3.0.5" % Test
)

scriptedLaunchOpts := scriptedLaunchOpts.value ++ Seq("-Xmx1024M", "-Dplugin.version=" + version.value)
scriptedBufferLog := false
