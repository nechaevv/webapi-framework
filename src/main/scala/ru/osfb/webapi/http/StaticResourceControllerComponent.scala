package ru.osfb.webapi.http

import akka.http.scaladsl.model.{StatusCodes, Uri}
import akka.http.scaladsl.server.Directives._
import ru.osfb.webapi.core.{ActorSystemComponent, ConfigurationComponent}

import scala.collection.JavaConversions._

/**
 * Created by sgl on 26.04.15.
 */
trait StaticResourceControllerComponent { this: ConfigurationComponent with ActorSystemComponent =>
  def staticResourceController = (get & pathEndOrSingleSlash) {
    redirect(Uri("index.html"), StatusCodes.Found)
  } ~ staticResourceRoutes.reduce(_ ~ _)

  lazy val staticResourceRoutes = configuration.getConfigList("static-resources").map(pathConfig =>
    (if (pathConfig.hasPath("prefix")) get & pathPrefix(pathConfig.getString("prefix")) else get) {
      getFromDirectory(pathConfig.getString("path"))
    }
  ).toList

}
