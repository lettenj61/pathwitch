import sbtcrossproject.CrossPlugin.autoImport.{ crossProject, CrossType }

lazy val sharedSettings = Seq(
  crossScalaVersions := Seq("2.11.12", "2.12.4"),
  scalaVersion := "2.12.4"
)

lazy val pathwitch = crossProject(JSPlatform, JVMPlatform)
  .in(file("."))
  .settings(sharedSettings)
  .settings(
    organization := "io.github.node4s",
    version := "0.1.0-SNAPSHOT",
    name := "pathwitch",
    libraryDependencies += "com.lihaoyi" %%% "utest" % "0.6.3" % "test",
    testFrameworks += new TestFramework("utest.runner.Framework")
  ).jsSettings(
    libraryDependencies += "io.scalajs" %%% "nodejs" % "0.4.2" % "test"
  ).jvmSettings(
    libraryDependencies += "com.github.pathikrit" %% "better-files" % "3.5.0" % "test",
    fork in Test := true,
    envVars in Test := sys.env.filterKeys(_ startsWith "PATHWITCH")
  )

lazy val pathwitchJS = pathwitch.js
lazy val pathwitchJVM = pathwitch.jvm
