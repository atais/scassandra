package com.github.atais.cassandra

import java.util.concurrent.Executor

import com.datastax.driver.core._
import com.google.common.base
import com.google.common.util.concurrent
import com.google.common.util.concurrent.{Futures, ListenableFuture}

import scala.language.implicitConversions

object ScalaIntegration {

  implicit class CqlStrings(val context: StringContext) extends AnyVal {
    def cql(args: Any*)(implicit session: Session): ListenableFuture[PreparedStatement] = {
      val statement = new SimpleStatement(context.raw(args: _*))
      session.prepareAsync(statement)
    }
  }

  private def bind(statement: ListenableFuture[PreparedStatement], params: Any*)
          (implicit session: Session, executor: Executor): ListenableFuture[BoundStatement] =
    Futures.transform[PreparedStatement, BoundStatement](statement,
      new base.Function[PreparedStatement, BoundStatement]() {
        override def apply(input: PreparedStatement): BoundStatement =
          input.bind(params.map(_.asInstanceOf[Object]): _*)
      }, executor)


  def execute(statement: ListenableFuture[PreparedStatement], params: Any*)
             (implicit session: Session, executor: Executor): ListenableFuture[ResultSet] = {
    val bs = bind(statement, params: _*)
    Futures.transformAsync[BoundStatement, ResultSet](bs,
      new concurrent.AsyncFunction[BoundStatement, ResultSet]() {
        override def apply(input: BoundStatement): ResultSetFuture = {
          session.executeAsync(input)
        }
      }, executor)
  }

}