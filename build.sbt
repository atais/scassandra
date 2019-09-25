import Deps._

lazy val commonSettings = Seq(
  organization := "com.github.atais",
  scalaVersion := "2.12.10",
  crossScalaVersions := Seq("2.10.7", "2.11.12", "2.12.10", "2.13.0"),
  libraryDependencies ++= Seq(scalaTest, scalaCheck),
  version := "0.1.0"
).flatten

import xerial.sbt.Sonatype._

lazy val publishSettings = Seq(
  publishTo := sonatypePublishToBundle.value,
  sonatypeProfileName := "com.github.atais",
  publishMavenStyle := true,
  licenses := Seq("APL2" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt")),
  sonatypeProjectHosting := Some(GitHubHosting("atais", "scassandra", "atais.jr@gmail.com"))
).flatten

lazy val cassandraScala = (project in file("cassandra-scala"))
  .settings(
    commonSettings,
    publishSettings,
    name := "cassandra-scala",
    libraryDependencies += cassandra,
    libraryDependencies ++= (scalaBinaryVersion.value match {
      case "2.10" => docker("0.9.8")
      case _ => docker("0.9.9")
    })
  )
  .dependsOn(listenableFutureScala % "compile->compile;test->test")

lazy val listenableFutureScala = (project in file("listenable-future-scala"))
  .settings(
    commonSettings,
    publishSettings,
    name := "listenable-future-scala",
    libraryDependencies += guava
  )

lazy val scassandra = (project in file("."))
  .aggregate(
    cassandraScala,
    listenableFutureScala
  )
  .settings(
    commonSettings,
    skip in publish := true,
    skip in publishLocal := true,
    skip in publishM2 := true,
    cancelable in Global := true
  )