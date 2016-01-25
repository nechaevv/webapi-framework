package ru.osfb.webapi.security

import java.util.UUID

import scala.concurrent.Future

/**
  * Created by sgl on 25.01.16.
  */
trait AuthenticationService {
  def authenticate(login: String, password: String): Future[UUID]
}

trait AuthenticationServiceComponent {
  def authenticationService: AuthenticationService
}