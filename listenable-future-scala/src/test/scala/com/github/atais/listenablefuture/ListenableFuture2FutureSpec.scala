package com.github.atais.listenablefuture

import java.util.concurrent.{Executor, TimeUnit}

import com.github.atais.listenablefuture.ListenableFuture2Future._
import com.google.common.util.concurrent.Futures
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{FlatSpec, Matchers, Suite}
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

class ListenableFuture2FutureSpec extends FlatSpec with Matchers with ScalaFutures
  with ScalaCheckDrivenPropertyChecks {
  that: Suite =>

  protected implicit val executor: Executor = scala.concurrent.ExecutionContext.global
  protected implicit val executionContext: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  it should "listenableFutureToFuture success" in {
    forAll { n: Int =>
      val lf = Futures.immediateFuture(n)
      val f = listenableFutureToFuture(lf)

      f.futureValue should be(n)
    }
  }

  it should "listenableFutureToFuture failure" in {
    forAll { n: String =>
      val lf = Futures.immediateFailedFuture[String](new Throwable(n))
      val f = listenableFutureToFuture(lf)

      Try(f.futureValue) match {
        case Success(_) => fail("should have failed")
        case Failure(ex) => ex.getCause.getMessage should be(n)
      }
    }
  }

  it should "futureToListenableFuture success" in {
    forAll { n: String =>
      val f = Future.successful(n)
      val lf = futureToListenableFuture(f)

      lf.get() should be(n)
    }
  }

  it should "futureToListenableFuture success timeout" in {
    val n: String = "testValue"
    lazy val f = Future {
      Thread.sleep(100)
      n
    }
    lazy val lf = futureToListenableFuture(f)

    lf.cancel(true)
    lf.isCancelled should be(false)
    lf.isDone should be(false)
    lf.get(1, TimeUnit.SECONDS) should be(n)
  }

  it should "futureToListenableFuture success add listener" in {
    forAll { n: Option[String] =>
      @volatile var x: Option[String] = None
      val f = Future.successful(n)
      val lf = futureToListenableFuture(f)

      x should be(None)

      lf.addListener(new Runnable {
        override def run(): Unit = {
          x = n
        }
      }, executor)
      lf.get()
      Thread.sleep(100) // wait for the Runnable to finish

      x should be(n)
    }

  }

  it should "futureToListenableFuture failure" in {
    forAll { n: String =>
      val f = Future.failed(new Throwable(n))
      val lf = futureToListenableFuture(f)

      Try(lf.get) match {
        case Success(_) => fail("should have failed")
        case Failure(ex) => ex.getMessage should be(n)
      }
    }
  }

}
