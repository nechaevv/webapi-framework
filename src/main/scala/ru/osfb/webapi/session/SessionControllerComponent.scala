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
  implicit val sessionInfoWrites = Json.writes[SessionInfo]

  def sessionController = pathPrefix("session") {
    (post & path("login")) {
      import ru.osfb.webapi.http.PlayJsonMarshallers._
      entity(as[Credentials]) { credentials =>
        onComplete((for {
          userId <- authenticationService.authenticate(credentials.login, credentials.password)
          sessionInfo <- sessionManager.create(UserSession(userId, credentials.clientToken))
        } yield sessionInfo).withErrorLog(logger).withTimeLog(logger, "session/login")) {
          case Success(sessionInfo) => complete(sessionInfo)
          case Failure(ex: AuthenticationException) => reject(AuthorizationFailedRejection)
          case Failure(ex) =>
            logger.error("Authentication error", ex)
            complete(HttpResponse(StatusCodes.InternalServerError))
        }
      }
    } ~ (post & path("logout") & accessToken) { sessionTokenOpt =>
      for (sessionToken <- sessionTokenOpt) sessionManager.discard(sessionToken)
      complete(StatusCodes.NoContent)
    } ~ path("ping") { userSession { sess =>
      complete(StatusCodes.NoContent)
    } }
  }

}

case class Credentials(login: String, password: String, clientToken: Option[String])
