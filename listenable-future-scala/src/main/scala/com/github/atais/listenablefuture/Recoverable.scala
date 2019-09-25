package com.github.atais.listenablefuture

import scala.language.higherKinds

trait Recoverable[F[_]] {

  def recover[A, T <: Throwable](fa: F[A])(pf: PartialFunction[T, A])(implicit m: Manifest[T]): F[A]

  def recoverWith[A, T <: Throwable](fa: F[A])(pf: PartialFunction[T, F[A]])(implicit m: Manifest[T]): F[A]

}

