package com.github.atais.cassandra

import com.whisk.docker.{DockerContainer, DockerKit, DockerReadyChecker}
import com.whisk.docker.scalatest.DockerTestKit
import org.scalatest.Suite
import org.scalatest.time.{Milliseconds, Seconds, Span}

trait CassandraDocker extends DockerTestKit with DockerKit {
  self: Suite =>

  implicit val pc: PatienceConfig = PatienceConfig(Span(30, Seconds), Span(100, Milliseconds))

  private val cqlPort: Int = 9042

  val cassandraContainer: DockerContainer = DockerContainer("whisk/cassandra:2.1.8")
    .withReadyChecker(DockerReadyChecker.LogLineContains("Starting listening for CQL clients on"))
    .withPorts(cqlPort -> None)

  protected lazy val cassandraCqlPort: Int = cassandraContainer.getPorts().futureValue.apply(cqlPort)

  override def dockerContainers: List[DockerContainer] = cassandraContainer :: super.dockerContainers

}
