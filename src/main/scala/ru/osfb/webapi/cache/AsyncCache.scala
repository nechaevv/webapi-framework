package ru.osfb.webapi.cache

import scala.concurrent.Future

/**
 * Created by sgl on 28.06.15.
 */
trait AsyncCache[K, V] {
  def apply(key: K): Future[Option[V]]
  def +=(kv: (K, V)): Future[Unit]
  def -=(key: K): Future[Option[V]]
  def listen(listener: AnyRef): Unit
  def cached(key: K)(fn: => Future[V]): Future[V]
}
