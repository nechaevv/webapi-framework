package ru.osfb.webapi.session

import ru.osfb.webapi.cache.CacheFactoryComponent
import ru.osfb.webapi.core.ExecutionContextComponent
import ru.osfb.webapi.utils.RandomUtils

import scala.concurrent.Future
import scala.concurrent.duration._

/**
 * Created by sgl on 28.06.15.
 */
trait CachedSessionManagerComponentImpl extends SessionManagerComponent {
  this: RefreshTokensServiceComponent with CacheFactoryComponent with ExecutionContextComponent =>

  private val sessionCache = cacheFactory[String, UserSession]("sessions", 1.day, 10.minutes)

  class SessionManagerImpl extends SessionManager {

    override def create(session: UserSession): Future[SessionTokens] = {
      val key = RandomUtils.randomToken(32)
      val createOp = sessionCache += key -> session
      for {
        refreshTokenOpt <- session.deviceId.fold[Future[Option[String]]](Future.successful(None)) { deviceId =>
          refreshTokensService.create(session.userId, deviceId).map(Some(_))
        }
        _ <- createOp
      } yield SessionTokens(key, refreshTokenOpt)
    }

    override def find(accessToken: String): Future[Option[UserSession]] = sessionCache(accessToken)

    override def discard(accessToken: String): Future[Unit] = (sessionCache -= accessToken).map(_ => ())

    override def refresh(refreshToken: String, deviceId: String): Future[SessionTokens] = for {
      Some(userId) <- refreshTokensService.refresh(refreshToken, deviceId)
      tokens <- create(UserSession(userId, Some(deviceId)))
    } yield tokens

  }

}
