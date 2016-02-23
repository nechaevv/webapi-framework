package ru.osfb.webapi.session

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

    override def find(accessToken: String, clientToken: Option[String]): Future[Option[UserSession]] = sessionCache(accessToken)
        .map(_.filter(_.clientToken == clientToken))

    override def discard(accessToken: String): Future[Unit] = {
      sessionCache -= accessToken
    } map (_ => ())

    private lazy val sessionCache = cacheFactory[String, UserSession]("SESSIONS", tokenTtl, inactiveTtl)

    private lazy val tokenLength = configuration.getInt("session.token-length")
    private lazy val tokenTtl:Duration = configuration.getDuration("session.token-ttl")
    private lazy val inactiveTtl:Duration = configuration.getDuration("session.inactive-ttl")

  }

}
