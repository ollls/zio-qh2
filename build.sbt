import Dependencies._

ThisBuild / scalaVersion := "3.3.5"
ThisBuild / version := "0.1"
ThisBuild / versionScheme := Some("early-semver")

ThisBuild / licenses := List("Apache 2" -> java.net.URI.create("http://www.apache.org/licenses/LICENSE-2.0.txt").toURL)
ThisBuild / homepage := Some(url("https://github.com/ollls/zio-quartz-h2"))

ThisBuild / pomIncludeRepository := { _ => false }

Runtime / unmanagedClasspath += baseDirectory.value / "src" / "main" / "resources"

// Main project
lazy val root = (project in file("."))
  .settings(
    name := "zio-qh2-project",
    organization := "io.github.ollls",
    publish / skip := true,
    Compile / scalaSource := baseDirectory.value / "src" / "main" / "scala",
    Compile / mainClass := Some("Run"),
    libraryDependencies += scalaTest % Test,
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio" % "2.0.15",
      "dev.zio" %% "zio-streams" % "2.0.15",
      "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-core" % "2.35.2",
      "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-macros" % "2.35.2" % "compile-internal",
      "io.github.ollls" %% "zio-quartz-h2" % "0.7.1"
    )
  )
