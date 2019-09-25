package com.github.atais.cassandra

import java.util.concurrent.Executor

import com.datastax.driver.core.ColumnDefinitions.Definition
import com.datastax.driver.core.{BoundStatement, Cluster, DataType, Session}
import com.github.atais.cassandra.ScalaIntegration._
import com.github.atais.cassandra.ScalaIntegrationSpec._
import com.github.atais.listenablefuture.ListenableFutureScala._
import com.google.common.util.concurrent.MoreExecutors
import com.whisk.docker.impl.spotify.DockerKitSpotify
import org.scalatest.{FlatSpec, Matchers}

import scala.collection.JavaConverters._

class ScalaIntegrationSpec extends FlatSpec with Matchers
  with CassandraDocker with DockerKitSpotify {

  behavior of "ScalaIntegrationTest"
  protected implicit val executor: Executor = MoreExecutors.directExecutor()

  private lazy val cluster = Cluster.builder
    .withClusterName("testCluster")
    .addContactPoints("localhost")
    .withPort(cassandraCqlPort)
    .build

  private implicit lazy val session: Session = cluster.connect()

  private lazy val insertQuery = s"INSERT INTO $keyspace.$table ($tableKey) values (?)"
  private lazy val getCql = cql"SELECT * FROM $keyspace.$table"
  private lazy val insertCql = cql"$insertQuery"
  val testValue = "atais"

  it should "cql" in {
    insertCql.get.getQueryString should be(insertQuery)

    val cd: Definition = insertCql.get.getVariables.asList().asScala.head
    cd.getKeyspace should be(keyspace)
    cd.getTable should be(table)
    cd.getName should be(tableKey)
    cd.getType.getName should be(DataType.Name.VARCHAR)
  }

  it should "execute" in {
    val rs = ScalaIntegration.execute(insertCql, testValue)
      .flatMap(_ => ScalaIntegration.execute(getCql))
      .get()

    val o = rs.one()
    o.getString(tableKey) should be(testValue)
  }


  override def beforeAll(): Unit = {
    super.beforeAll()
    session.execute(s"CREATE KEYSPACE $keyspace WITH replication = {'class': 'SimpleStrategy', 'replication_factor': '1'};")
    session.execute(s"CREATE TABLE $keyspace.$table ($tableKey text PRIMARY KEY)")
  }
  override def afterAll(): Unit = {
    session.close()
    cluster.close()
    super.afterAll()
  }

}

object ScalaIntegrationSpec {

  protected val keyspace = "test"
  protected val tableKey = "key"
  protected val table = "example"

}