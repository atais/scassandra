package com.github.atais.listenablefuture

import java.util.concurrent.Executor

import com.google.common.base
import com.google.common.util.concurrent
import com.google.common.util.concurrent.{Futures, ListenableFuture}

import scala.concurrent.Future

object ListenableFutureScala extends ListenableFutureScala {

  implicit class ListenableFutureOps[I](val that: ListenableFuture[I]) extends AnyVal {

    def flatMap[O](f: I => ListenableFuture[O])
                  (implicit exe: Executor): ListenableFuture[O] =
      flatMapF(that)(f)

    def map[O](f: I => O)
              (implicit exe: Executor): ListenableFuture[O] =
      mapF(that)(f)

    def recover[T <: Throwable](f: T => I)
                               (implicit m: Manifest[T], exe: Executor): ListenableFuture[I] =
      recoverF(that)(f)

    def recoverWith[T <: Throwable](f: T => ListenableFuture[I])
                                   (implicit m: Manifest[T], exe: Executor): ListenableFuture[I] =
      recoverWithF(that)(f)

    def future: Future[I] =
      ListenableFuture2Future.listenableFutureToFuture(that)
  }

}

trait ListenableFutureScala {

  def sequence[A](fu: Iterable[ListenableFuture[A]])
                 (implicit exe: Executor): ListenableFuture[Iterable[A]] = {
    import scala.collection.JavaConverters._
    val java = Futures.allAsList(fu.asJava)
    mapF(java)(_.asScala)
  }

  protected def flatMapF[A, B](fa: ListenableFuture[A])(f: A => ListenableFuture[B])(implicit exe: Executor): ListenableFuture[B] =
    Futures.transformAsync[A, B](fa, new concurrent.AsyncFunction[A, B]() {
      override def apply(input: A): ListenableFuture[B] = f(input)
    }, exe)

  protected def mapF[A, B](fa: ListenableFuture[A])(f: A => B)(implicit exe: Executor): ListenableFuture[B] =
    Futures.transform[A, B](fa, new base.Function[A, B] {
      override def apply(input: A): B = f(input)
    }, exe)

  protected def recoverF[A, T <: Throwable](fa: ListenableFuture[A])(f: T => A)
                                           (implicit m: Manifest[T], exe: Executor): ListenableFuture[A] =
    Futures.catching(fa, m.runtimeClass.asInstanceOf[Class[T]], new base.Function[T, A] {
      override def apply(input: T): A =
        f(input)
    }, exe)

  protected def recoverWithF[A, T <: Throwable](fa: ListenableFuture[A])(f: T => ListenableFuture[A])
                                               (implicit m: Manifest[T], exe: Executor): ListenableFuture[A] =
    Futures.catchingAsync(fa, m.runtimeClass.asInstanceOf[Class[T]], new concurrent.AsyncFunction[T, A] {
      override def apply(input: T): ListenableFuture[A] =
        f(input)
    }, exe)

}
