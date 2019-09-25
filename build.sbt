import Deps._

lazy val commonSettings = Seq(
  organization := "com.github",
  scalaVersion := "2.12.10",
  crossScalaVersions := Seq("2.10.7", "2.11.12", "2.12.10", "2.13.0"),
  libraryDependencies ++= Seq(scalaTest, scalaCheck)
).flatten

lazy val cassandraScala = (project in file("cassandra-scala"))
  .settings(
    commonSettings,
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
    name := "listenable-future-scala",
    libraryDependencies += guava
  )

lazy val scassandra = (project in file("."))
  .aggregate(
    cassandraScala,
    listenableFutureScala
  )
  .settings(
    skip in publish := true,
    skip in publishLocal := true,
    skip in publishM2 := true,
    cancelable in Global := true
  )