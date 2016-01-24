package ru.osfb.webapi.session

import java.time.Instant
import java.util.UUID

import ru.osfb.webapi.db.{ColumnTypes, DatabaseDriverComponent}

/**
 * Created by sgl on 14.10.15.
 */
trait RefreshTokensModel extends ColumnTypes { this: DatabaseDriverComponent =>
  import databaseDriver.api._

  class RefreshTokensTable(t: Tag) extends Table[RefreshToken](t, "REFRESH_TOKENS") {
    def token = column[String]("TOKEN")
    def deviceId = column[String]("DEVICE_ID")
    def userId = column[UUID]("USER_ID")
    def createdOn = column[Instant]("CREATED_ON")
    def * = (token, deviceId, userId, createdOn) <> (RefreshToken.tupled, RefreshToken.unapply)
  }

  val refreshTokens = TableQuery[RefreshTokensTable]

}

case class RefreshToken(token: String, deviceId: String, userId: UUID, createdOn: Instant)
