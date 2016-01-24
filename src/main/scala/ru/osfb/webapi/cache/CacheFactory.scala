package ru.osfb.webapi.cache

import scala.concurrent.duration.Duration

/**
 * Created by sgl on 28.06.15.
 */
trait CacheFactory {
  def apply[K, V](name: String, entryTtl: Duration, entryIdleTtl: Duration = Duration.Inf): AsyncCache[K, V]
}

trait CacheFactoryComponent {
  def cacheFactory: CacheFactory
}