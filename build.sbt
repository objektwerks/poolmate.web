name := "poolmate.web"

lazy val laminarVersion = "0.14.2"
lazy val scalaJavaTimeVersion = "2.3.0"
lazy val upickleVersion = "1.4.3"
lazy val scalaTestVersion = "3.2.10"

lazy val common = Defaults.coreDefaultSettings ++ Seq(
  organization := "objektwerks",
  version := "0.1-SNAPSHOT",
  scalaVersion := "3.1.0"
)

lazy val pool = project.in(file("."))
  .aggregate(sharedJs, sharedJvm, js, jvm)
  .settings(common)
  .settings(
    publish := {},
    publishLocal := {}
  )

lazy val shared = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("shared"))
  .settings(common)
  .settings(
    libraryDependencies ++= Seq(
      "com.lihaoyi" %% "upickle" % upickleVersion,
      "io.github.cquiroz" %% "scala-java-time" % scalaJavaTimeVersion,
      "org.scalatest" %% "scalatest" % scalaTestVersion % Test
    )
  )

lazy val sharedJs = shared.js
lazy val sharedJvm = shared.jvm

lazy val js = (project in file("js"))
  .dependsOn(sharedJs)
  .enablePlugins(ScalaJSPlugin, UniversalPlugin)
  .settings(common)
  .settings(
    libraryDependencies ++= Seq(
      "com.raquo" %%% "laminar" % laminarVersion,
      "com.lihaoyi" %%% "upickle" % upickleVersion,
      "io.github.cquiroz" %%% "scala-java-time" % scalaJavaTimeVersion
    )
  )

lazy val jvm = (project in file("jvm"))
  .dependsOn(sharedJvm)
  .enablePlugins(JavaServerAppPackaging)
  .configs(IntegrationTest)
  .settings(common)
  .settings(
    Defaults.itSettings,
    reStart / mainClass := Some("pool.Server"),
    libraryDependencies ++= {
      Seq(
        "com.typesafe" % "config" % "1.4.1",
        "com.typesafe.scala-logging" %% "scala-logging" % "3.9.4",
        "ch.qos.logback" % "logback-classic" % "1.2.10",
        "org.scalatest" %% "scalatest" % scalaTestVersion % IntegrationTest
      )
    }
  )
