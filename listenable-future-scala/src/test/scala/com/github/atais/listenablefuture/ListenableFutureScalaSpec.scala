package com.github.atais.listenablefuture

import java.util.concurrent.Executor

import com.github.atais.listenablefuture.ListenableFutureScala._
import com.google.common.util.concurrent.{Futures, ListenableFuture, MoreExecutors}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{FlatSpec, Matchers}
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

import scala.util.{Failure, Success, Try}

class ListenableFutureScalaSpec extends FlatSpec with Matchers with ScalaFutures
  with ScalaCheckDrivenPropertyChecks {

  protected implicit val executor: Executor = MoreExecutors.directExecutor()

  it should "flatMap" in {
    forAll { n: Int =>
      val func = (i: Int) => i + 1

      val f = Futures.immediateFuture(n)
      val g = f.flatMap(i => Futures.immediateFuture(func(i)))
      g.get() should be(func(n))
    }
  }

  it should "map" in {
    forAll { n: Int =>
      val func = (i: Int) => i + 1

      val f = Futures.immediateFuture(n)
      val g = f.map(func)
      g.get() should be(func(n))
    }
  }

  for {
    text <- Futures.immediateFuture("abc")
    number <- Futures.immediateFuture(123)
  } yield {
    text + number
  }

  val c: ListenableFuture[Int] = Futures.immediateFuture("abc")
    .flatMap(text => Futures.immediateFuture(text.length))
    .flatMap(i => Futures.immediateCancelledFuture())


  it should "sequence" in {
    forAll { n: Seq[Int] =>
      val fs = n.map(Futures.immediateFuture[Int])
      val f = sequence(fs)

      f.get() should be(n)
    }
  }

  it should "convert to future" in {
    forAll { n: Int =>
      val lf = Futures.immediateFuture(n)
      lf.future.futureValue should be(n)
    }
  }

  it should "recoverWith" in {
    forAll { n: Int =>
      val f = Futures.immediateFailedFuture[Int](new RuntimeException())
      val g = f.recoverWith[Exception] { _: Exception => Futures.immediateFuture(n) }
      g.get() should be(n)
    }
  }

  it should "recover" in {
    forAll { n: Int =>
      val f = Futures.immediateFailedFuture[Int](new RuntimeException())
      val g = f.recover[Exception] { _: Exception => n }
      g.get() should be(n)
    }
  }

  private val ex = new ArrayIndexOutOfBoundsException()

  it should "not recover" in {
    forAll { n: Int =>
      Try {
        Futures.immediateFuture(n)
          .map[Int](_ => throw ex)
          .recover[UnsupportedOperationException] {
            _: UnsupportedOperationException => n // should not work, wrong exception
          }
          .get()
      } match {
        case Success(_) => fail("should have failed")
        case Failure(x) => x.getCause should be(ex)
      }
    }
  }


}
