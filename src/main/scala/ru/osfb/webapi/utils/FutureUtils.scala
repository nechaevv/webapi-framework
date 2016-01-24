package ru.osfb.webapi.utils

import com.typesafe.scalalogging.Logger

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

/**
 * Created by sgl on 02.08.15.
 */
object FutureUtils {
  class PimpedFuture[T](f: Future[T]) {
    def withErrorLog(logger: Logger)(implicit executionContext: ExecutionContext): Future[T] = {
      f onFailure {
        case NonFatal(e) => logger.error("Async operation failed", e)
      }
      f
    }
    def withTimeLog(logger: Logger, name: String)(implicit executionContext: ExecutionContext): Future[T] = {
      val start = System.currentTimeMillis()
      f onComplete { _ =>
        logger.trace(name + " execution time: " + (System.currentTimeMillis() - start).toString + " ms")
      }
      f
    }
  }
  implicit def enrichFuture[T](f: Future[T]): PimpedFuture[T] = new PimpedFuture(f)
}
