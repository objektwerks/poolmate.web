name := "work.order.scala"

lazy val caskVersion = "0.8.3"
lazy val laminarVersion = "0.14.5"
lazy val waypointVersion = "0.5.0"
lazy val upickleVersion = "2.0.0"
lazy val mysqlVersion = "8.0.30"
lazy val scalaJavaTimeVersion = "2.4.0"
lazy val scalaTestVersion = "3.2.14"

lazy val common = Defaults.coreDefaultSettings ++ Seq(
  organization := "objektwerks",
  version := "0.1-SNAPSHOT",
  scalaVersion := "3.2.2-RC1",
  parallelExecution := false
)

lazy val poolmate = project.in(file("."))
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
lazy val public = "public"

lazy val js = (project in file("js"))
  .dependsOn(sharedJs)
  .enablePlugins(ScalaJSPlugin)
  .settings(common)
  .settings(
    libraryDependencies ++= Seq(
      "com.raquo" %%% "laminar" % laminarVersion,
      "com.raquo" %%% "waypoint" % waypointVersion,
      "com.lihaoyi" %%% "upickle" % upickleVersion,
      "io.github.cquiroz" %%% "scala-java-time" % scalaJavaTimeVersion
    ),
    Compile / fastLinkJS / scalaJSLinkerOutputDirectory := target.value / public,
    Compile / fullLinkJS / scalaJSLinkerOutputDirectory := target.value / public
  )

lazy val jvm = (project in file("jvm"))
  .dependsOn(sharedJvm)
  .enablePlugins(JavaServerAppPackaging)
  .settings(common)
  .settings(
    reStart / mainClass := Some("workorder.Server"),
    libraryDependencies ++= {
      Seq(
        "com.lihaoyi" %% "cask" % caskVersion,
        "com.lihaoyi" %% "upickle" % upickleVersion,
        "org.scalikejdbc" %% "scalikejdbc" % "4.0.0",
        "mysql" % "mysql-connector-java" % mysqlVersion,
        "io.github.cquiroz" %% "scala-java-time" % "2.3.0",
        "com.github.blemale" %% "scaffeine" % "5.2.0",
        "org.jodd" % "jodd-mail" % "6.0.5",
        "com.typesafe" % "config" % "1.4.2",
        "com.typesafe.scala-logging" %% "scala-logging" % "3.9.5",
        "ch.qos.logback" % "logback-classic" % "1.4.4",
        "org.scalatest" %% "scalatest" % scalaTestVersion % Test
      )
    }
  )
