package ru.osfb.webapi.http

import akka.http.scaladsl.marshalling.{Marshaller, ToEntityMarshaller}
import akka.http.scaladsl.model.MediaTypes.`application/json`
import akka.http.scaladsl.model.{HttpCharsets, MediaTypes}
import akka.http.scaladsl.unmarshalling.{FromEntityUnmarshaller, Unmarshaller}
import akka.stream.Materializer
import play.api.libs.json._

import scala.language.implicitConversions

/**
 * Created by sgl on 02.08.15.
 */
object PlayJsonMarshallers {

  implicit def playFromJsonUnmarshaller[T](implicit r: Reads[T], mat: Materializer): FromEntityUnmarshaller[T] = {
    playJsValueUnmarshaller map(_.as[T])
  }

  implicit def playJsValueUnmarshaller(implicit mat: Materializer): FromEntityUnmarshaller[JsValue] = {
    Unmarshaller.byteStringUnmarshaller.forContentTypes(`application/json`).mapWithCharset { (data, charset) ⇒
      if (charset == HttpCharsets.`UTF-8`) Json.parse(data.toArray)
      else Json.parse(data.decodeString(charset.nioCharset.name))
    }
  }

  implicit def playToJsonMarshaller[T](implicit w: Writes[T]): ToEntityMarshaller[T] = {
    playJsValueMarshaller compose w.writes
  }

  implicit def playJsValueMarshaller: ToEntityMarshaller[JsValue] = {
    Marshaller.StringMarshaller.wrap(MediaTypes.`application/json`)(Json.stringify)
  }

}
