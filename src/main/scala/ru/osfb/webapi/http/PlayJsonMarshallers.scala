package ru.osfb.webapi.http

import scala.language.implicitConversions
import akka.stream.Materializer
import akka.http.scaladsl.marshalling.{ ToEntityMarshaller, Marshaller }
import akka.http.scaladsl.unmarshalling.{ FromEntityUnmarshaller, Unmarshaller }
import akka.http.scaladsl.model.{ ContentTypes, HttpCharsets }
import akka.http.scaladsl.model.MediaTypes.`application/json`
import play.api.libs.json._

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
    Marshaller.StringMarshaller.wrap(ContentTypes.`application/json`)(Json.stringify)
  }

}
