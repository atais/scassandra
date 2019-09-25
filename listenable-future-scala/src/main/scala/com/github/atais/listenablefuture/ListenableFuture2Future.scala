package com.github.atais.listenablefuture

import java.util.concurrent.{Executor, TimeUnit}

import com.google.common.util.concurrent.{FutureCallback, Futures, ListenableFuture}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future, Promise}

object ListenableFuture2Future extends ListenableFuture2Future

trait ListenableFuture2Future {

  def listenableFutureToFuture[T](listenableFuture: ListenableFuture[T]): Future[T] = {
    val promise = Promise[T]()
    Futures.addCallback(listenableFuture, new FutureCallback[T] {
      def onFailure(error: Throwable): Unit =
        promise.failure(error)

      def onSuccess(result: T): Unit =
        promise.success(result)
    })
    promise.future
  }

  def futureToListenableFuture[T](future: Future[T]): ListenableFuture[T] =
    new ListenableFuture[T] {

      override def addListener(listener: Runnable, executor: Executor): Unit =
        future.onComplete(_ => listener.run())(ExecutionContext.fromExecutor(executor))

      override def cancel(mayInterruptIfRunning: Boolean): Boolean =
        false

      override def isCancelled: Boolean =
        false

      override def isDone: Boolean =
        future.isCompleted

      override def get(): T =
        Await.result(future, Duration.Inf)

      override def get(timeout: Long, unit: TimeUnit): T =
        Await.result(future, Duration(timeout, unit))
    }

}
