package ru.osfb.webapi.session

import scala.concurrent.Future

/**
 * Created by sgl on 28.06.15.
 */
trait SessionManagerService {
  def create(userSession: UserSession): Future[SessionInfo]
  def find(accessToken: String, clientToken: Option[String]): Future[Option[UserSession]]
  def discard(accessToken: String): Future[Unit]
}

trait SessionManagerComponent {
  def sessionManager: SessionManagerService
}

case class SessionInfo(accessToken: String, tokenTtl: Int, inactiveTtl: Int)