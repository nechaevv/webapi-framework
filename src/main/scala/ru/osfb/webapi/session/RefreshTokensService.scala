package ru.osfb.webapi.session

import java.time.Instant
import java.util.UUID

import ru.osfb.webapi.core.ExecutionContextComponent
import ru.osfb.webapi.db.{DatabaseComponent, DatabaseDriverComponent}
import ru.osfb.webapi.utils.RandomUtils

import scala.concurrent.Future

/**
 * Created by sgl on 14.10.15.
 */
trait RefreshTokensService {
  def refresh(token: String, deviceId: String): Future[Option[UUID]]
  def create(userId: UUID, deviceId: String): Future[String]
  def deleteAll(userId: UUID): Future[Unit]
}

trait RefreshTokensServiceComponent {
  def refreshTokensService: RefreshTokensService
}

trait RefreshTokensServiceComponentImpl extends RefreshTokensServiceComponent {
  this: DatabaseDriverComponent with DatabaseComponent with RefreshTokensModel with ExecutionContextComponent =>
  class RefreshTokensServiceImpl extends RefreshTokensService {
    import databaseDriver.api._
    override def refresh(token: String, deviceId: String): Future[Option[UUID]] = {
      database.run(for {
        userIdOpt <- refreshTokens.filter(t => t.token === token && t.deviceId === deviceId)
          .map(_.userId).result.headOption
        _ <- refreshTokens.filter(_.deviceId === deviceId).delete
      } yield userIdOpt)
    }

    override def create(userId: UUID, deviceId: String): Future[String] = {
      val newToken = RandomUtils.randomToken(64)
      database.run(
        refreshTokens.filter(_.deviceId === deviceId).delete andThen
        (refreshTokens += RefreshToken(newToken, deviceId, userId, Instant.now()))
      ) map (_ => newToken)
    }

    override def deleteAll(userId: UUID): Future[Unit] = database.run(
      refreshTokens.filter(_.userId === userId).delete
    ).map(_ => ())

  }
}