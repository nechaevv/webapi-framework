package ru.osfb.webapi.session

import play.api.libs.json.Json

import scala.concurrent.Future

/**
 * Created by sgl on 28.06.15.
 */
trait SessionManager {
  def create(userSession: UserSession): Future[SessionTokens]
  def find(accessToken: String): Future[Option[UserSession]]
  def discard(accessToken: String): Future[Unit]
  def refresh(refreshToken: String, deviceId: String): Future[SessionTokens]
}

case class SessionTokens(accessToken: String, refreshToken: Option[String])

trait SessionManagerComponent {
  def sessionManager: SessionManager
  implicit val sessionTokensWrites = Json.writes[SessionTokens]
}