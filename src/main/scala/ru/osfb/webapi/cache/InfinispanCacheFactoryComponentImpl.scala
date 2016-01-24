package ru.osfb.webapi.cache

import java.util.concurrent
import java.util.concurrent.TimeUnit

import org.infinispan.Cache
import org.infinispan.commons.util.concurrent.{FutureListener, NotifyingFuture}
import org.infinispan.manager.DefaultCacheManager
import ru.osfb.webapi.core.ExecutionContextComponent

import scala.concurrent.duration.Duration
import scala.concurrent.{Future, Promise}
import scala.language.implicitConversions
import scala.util.Try

/**
 * Created by sgl on 28.06.15.
 */
trait InfinispanCacheFactoryComponentImpl extends CacheFactoryComponent { this: ExecutionContextComponent =>
  private implicit def futureAdapter[T](nf: NotifyingFuture[T]): Future[T] = {
    val promise = Promise[T]()
    nf.attachListener(new FutureListener[T] {
      override def futureDone(future: concurrent.Future[T]): Unit = promise.complete(Try(future.get()))
    })
    promise.future
  }

  class InfinispanCache[K, V](provider: Cache[K, V], entryTtl: Duration, entryIdleTtl: Duration) extends AsyncCache[K, V] {
    override def apply(key: K): Future[Option[V]] = provider.getAsync(key).map(Option.apply)
    override def -=(key: K): Future[Option[V]] = provider.removeAsync(key).map(Option.apply)
    override def +=(kv: (K, V)): Future[Unit] = (entryIdleTtl match {
      case Duration.Inf => provider.putAsync(kv._1, kv._2, entryTtl.toMillis, TimeUnit.MILLISECONDS)
      case idleTtl => provider.putAsync(kv._1, kv._2, entryTtl.toMillis, TimeUnit.MILLISECONDS,
        idleTtl.toMillis, TimeUnit.MILLISECONDS)
    }).map(_ => ())

  }

  class InfinispanCacheFactory extends CacheFactory {
    private val manager = new DefaultCacheManager("infinispan.xml")
    override def apply[K, V](name: String, entryTtl: Duration, entryIdleTtl: Duration = Duration.Inf): AsyncCache[K, V] = {
      new InfinispanCache[K, V](manager.getCache(name), entryTtl, entryIdleTtl)
    }
  }

}
