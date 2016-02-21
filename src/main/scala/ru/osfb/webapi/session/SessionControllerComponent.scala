package ru.osfb.webapi.session

import akka.http.scaladsl.marshalling.Marshaller._
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.server.AuthorizationFailedRejection
import akka.http.scaladsl.server.Directives._
import com.typesafe.scalalogging.LazyLogging
import play.api.libs.json.Json
import ru.osfb.webapi.core.{ActorMaterializerComponent, ExecutionContextComponent}
import ru.osfb.webapi.security.{AuthenticationException, AuthenticationServiceComponent}
import ru.osfb.webapi.utils.FutureUtils._

import scala.util.{Failure, Success}

/**
 * Created by sgl on 25.04.15.
 */
trait SessionControllerComponent extends SessionDirectives with LazyLogging {
  this: SessionManagerComponent
    with AuthenticationServiceComponent
    with ExecutionContextComponent
    with ActorMaterializerComponent =>

  implicit val credentialsReads = Json.reads[Credentials]
  implicit val refreshRequestReads = Json.reads[RefreshRequest]

  def sessionController = pathPrefix("session") {
    (post & path("login")) {
      import ru.osfb.webapi.http.PlayJsonMarshallers._
      entity(as[Credentials]) { credentials =>
        onComplete((for {
          userId <- authenticationService.authenticate(credentials.login, credentials.password)
          tokens <- sessionManager.create(UserSession(userId, credentials.clientToken))
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
        tokens <- sessionManager.refresh(req.refreshToken, req.clientToken)
      } yield tokens).withErrorLog(logger).withTimeLog(logger, "session/refresh")) {
          case Success(tokens) => complete(tokens)
          case Failure(ex: NoSuchElementException) => reject(AuthorizationFailedRejection)
          case Failure(ex) =>
            logger.error("Authentication refresh error", ex)
            complete(HttpResponse(StatusCodes.InternalServerError))
      }
    } } ~ (post & path("logout") & accessToken) { sessionTokenOpt =>
      for (sessionToken <- sessionTokenOpt) sessionManager.discard(sessionToken)
      complete(StatusCodes.NoContent)
    } ~ path("ping") { userSession { sess =>
      complete(StatusCodes.NoContent)
    } }
  }

}

case class Credentials(login: String, password: String, clientToken: Option[String])
case class RefreshRequest(refreshToken: String, clientToken: String)