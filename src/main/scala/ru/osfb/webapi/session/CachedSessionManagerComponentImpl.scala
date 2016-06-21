package ru.osfb.webapi.session

import java.util.UUID

import org.infinispan.notifications.Listener
import org.infinispan.notifications.cachelistener.annotation.{CacheEntryCreated, CacheEntryExpired, CacheEntryRemoved}
import org.infinispan.notifications.cachelistener.event.{CacheEntryCreatedEvent, CacheEntryExpiredEvent, CacheEntryRemovedEvent}
import ru.osfb.webapi.cache.CacheFactoryComponent
import ru.osfb.webapi.core.{ConfigurationComponent, ExecutionContextComponent}
import ru.osfb.webapi.utils.RandomUtils

import scala.concurrent.Future
import scala.concurrent.duration._

/**
 * Created by sgl on 28.06.15.
 */
trait CachedSessionManagerComponentImpl extends SessionManagerComponent {
  this: CacheFactoryComponent
    with ExecutionContextComponent
    with ConfigurationComponent =>

  class SessionManagerServiceImpl extends SessionManagerService {

    override def create(session: UserSession): Future[SessionInfo] = for {
      key <- Future { RandomUtils.randomToken(tokenLength) }
      _ <- sessionCache += key -> session
    } yield SessionInfo(key, tokenTtl.toMillis.toInt, inactiveTtl.toMillis.toInt)

    override def find(accessToken: String, clientToken: Option[String]): Future[Option[UserSession]] = {
      val result = sessionCache(accessToken).map(_.filter(_.clientToken == clientToken))
      result.onSuccess { // prolong user sessions cache
        case Some(userSession) => userSessionsCache(userSession.userId)
      }
      result
    }

    override def discard(accessToken: String): Future[Unit] = {
      sessionCache -= accessToken
    } map (_ => ())

    override def discardAll(userId: UUID): Future[Unit] = userSessionsCache(userId) flatMap {
      case Some(userSessions) => Future.sequence(userSessions map discard).map(_ => ())
      case None => Future.successful()
    }

    protected lazy val sessionCache = {
      val cache = cacheFactory[String, UserSession]("SESSIONS", tokenTtl, inactiveTtl)
      cache.listen(new SessionCacheListener)
      cache
    }
    protected lazy val userSessionsCache = cacheFactory[UUID, Seq[String]]("USER_SESSIONS", tokenTtl, inactiveTtl)

    protected lazy val tokenLength = configuration.getInt("session.token-length")
    protected lazy val tokenTtl:Duration = configuration.getDuration("session.token-ttl")
    protected lazy val inactiveTtl:Duration = configuration.getDuration("session.inactive-ttl")

    @Listener(clustered = true)
    class SessionCacheListener {
      @CacheEntryCreated
      def onCreated(ev: CacheEntryCreatedEvent[String, UserSession]): Unit = {
        userSessionsCache(ev.getValue.userId) foreach {
          case Some(userSessions) => userSessionsCache += ev.getValue.userId -> (userSessions :+ ev.getKey)
          case None => userSessionsCache += ev.getValue.userId -> Seq(ev.getKey)
        }
      }
      def sessionRemoved(sessionKey: String, userId: UUID): Unit = {
        userSessionsCache(userId) foreach {
          case Some(userSessions) => userSessionsCache += userId -> userSessions.filter(_ != sessionKey)
          case None => // do nothing
        }
      }
      @CacheEntryRemoved
      def onRemoved(ev: CacheEntryRemovedEvent[String, UserSession]): Unit = sessionRemoved(ev.getKey, ev.getValue.userId)
      @CacheEntryExpired
      def onExpired(ev: CacheEntryExpiredEvent[String, UserSession]): Unit = sessionRemoved(ev.getKey, ev.getValue.userId)
    }

  }

}
