package ru.osfb.webapi.session

import akka.http.scaladsl.model.headers.{OAuth2BearerToken, Authorization}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import ru.osfb.webapi.core.ExecutionContextComponent

/**
  * Created by sgl on 21.02.16.
  */
trait SessionDirectives { this: SessionManagerComponent with ExecutionContextComponent =>

  val accessToken: Directive1[Option[String]] = optionalHeaderValueByType[Authorization]() map (_.collect {
    case Authorization(OAuth2BearerToken(token)) => token
  })

  val clientToken: Directive1[Option[String]] = optionalHeaderValueByName("Client-Token")

  val userSession: Directive1[UserSession] = (accessToken & clientToken) tflatMap {
    case (Some(access), client) => onSuccess(
      sessionManager.find(access, client)
    ) flatMap {
      case Some(s) => provide(s)
      case None => reject(AuthorizationFailedRejection)
    }
    case _ => reject(AuthorizationFailedRejection)
  }

}
