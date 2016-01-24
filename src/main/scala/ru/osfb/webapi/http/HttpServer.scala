package ru.osfb.webapi.http

import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory
import ru.osfb.webapi.core.{ConfigurationComponent, ActorSystemComponent, ExecutionContextComponent}

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.util.{Failure, Success}

/**
 * Created by sgl on 25.04.15.
 */

trait HttpServer {
  def start(route: Route)
  def stop()
}

trait HttpServerComponent {
  def httpServer: HttpServer
}

trait HttpServerComponentImpl extends HttpServerComponent { this: ConfigurationComponent with ActorSystemComponent with MaterializerComponent
  with ExecutionContextComponent =>

  class HttpServerImpl extends HttpServer {

    lazy val log = Logger(LoggerFactory.getLogger(classOf[HttpServer]))

    lazy val interface = configuration.getString("http.interface")
    lazy val port = configuration.getInt("http.port")
    var binding: Future[Http.ServerBinding] = _

    override def start(route: Route): Unit = {
      val serverSource = Http().bind(interface, port)
      val connectionHandler = Route.handlerFlow(route)
      binding = serverSource.to(Sink.foreach { conn =>
        log.trace(s"HTTP connection from ${conn.remoteAddress}")
        conn.handleWith(connectionHandler)
      }).run()

      binding.onComplete {
        case Success(_) => log.trace("HTTP server started")
        case Failure(err) => log.error("HTTP server startup error", err)
      }

    }

    override def stop(): Unit = {
      val serverBinding = Await.result(binding, 1.minute)
      Await.result(serverBinding.unbind(), 1.minute)
      log.trace("HTTP server stopped")
    }

  }

}
