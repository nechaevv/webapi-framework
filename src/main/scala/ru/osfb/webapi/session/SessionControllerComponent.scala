package ru.osfb.webapi.session

import akka.http.scaladsl.marshalling.Marshaller._
import akka.http.scaladsl.model.headers.{Authorization, OAuth2BearerToken}
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{AuthorizationFailedRejection, Directive1}
import ru.osfb.webapi.core.{ExecutionContextComponent, ActorMaterializerComponent}
import ru.osfb.webapi.utils.FutureUtils._
import com.typesafe.scalalogging.LazyLogging
import play.api.libs.json.Json
import ru.osfb.webapi.core.{ActorMaterializerComponent, ExecutionContextComponent}

import scala.util.{Failure, Success}

/**
 * Created by sgl on 25.04.15.
 */
trait SessionControllerComponent extends LazyLogging {
  this: SessionManagerComponent
    with ExecutionContextComponent
    with ActorMaterializerComponent =>

  val sessionToken: Directive1[Option[String]] = optionalHeaderValueByType[Authorization]() map (_.collect {
    case Authorization(OAuth2BearerToken(accessToken)) => accessToken
  })

  val userSession: Directive1[UserSession] = sessionToken flatMap {
    case Some(accessToken) => onSuccess(
      sessionManager.find(accessToken)
    ) flatMap {
      case Some(s) => provide(s)
      case None => reject(AuthorizationFailedRejection)
    }
    case None => reject(AuthorizationFailedRejection)
  }

  implicit val credentialsReads = Json.reads[Credentials]
  implicit val refreshRequestReads = Json.reads[RefreshRequest]

  def sessionController = pathPrefix("session") {
    (post & path("login")) {
      import ru.osfb.webapi.http.PlayJsonMarshallers._
      entity(as[Credentials]) { credentials =>
        onComplete((for {
          user <- usersService.authenticate(credentials.login, credentials.password)
          tokens <- sessionManager.create(UserSession(user, credentials.deviceId))
        } yield tokens).withErrorLog(logger).withTimeLog(logger, "session/login")) {
          case Success(tokens) => complete(tokens)
          case Failure(ex: AuthenticationException) => reject(AuthorizationFailedRejection)
          case Failure(ex) =>
            logger.error("Authentication error", ex)
            complete(HttpResponse(StatusCodes.InternalServerError))
        }
      }
    } ~ (post & path("refresh")) {
      import ru.osfb.webapi.http.PlayJsonMarshallers._
      entity(as[RefreshRequest]) { req => onComplete((for {
        tokens <- sessionManager.refresh(req.refreshToken, req.deviceId)
      } yield tokens).withErrorLog(logger).withTimeLog(logger, "session/refresh")) {
          case Success(tokens) => complete(tokens)
          case Failure(ex: NoSuchElementException) => reject(AuthorizationFailedRejection)
          case Failure(ex) =>
            logger.error("Authentication refresh error", ex)
            complete(HttpResponse(StatusCodes.InternalServerError))
      }
    } } ~ (post & path("logout") & sessionToken) { sessionTokenOpt =>
      for (sessionToken <- sessionTokenOpt) sessionManager.discard(sessionToken)
      complete(StatusCodes.NoContent)
    } ~ path("ping") { userSession { sess =>
      complete(StatusCodes.NoContent)
    } }
  }

}

case class Credentials(login: String, password: String, deviceId: Option[String])
case class RefreshRequest(refreshToken: String, deviceId: String)