import sbt._

object Deps {

  val cassandra = "com.datastax.cassandra" % "cassandra-driver-core" % "3.2.0"
  val guava = "com.google.guava" % "guava" % "19.0" // minimal guava version, from cassandra

  val scalaTest = "org.scalatest" %% "scalatest" % "3.0.8" % Test
  val scalaCheck = "org.scalacheck" %% "scalacheck" % "1.14.0" % Test

  def docker(version: String): Seq[ModuleID] = {
    Seq("docker-testkit-scalatest", "docker-testkit-impl-spotify")
      .map("com.whisk" %% _ % version % Test) :+
      // https://github.com/eclipse-ee4j/jaxb-ri/issues/1222
      "javax.xml.bind" % "jaxb-api" % "2.3.1" % Test
  }

}
