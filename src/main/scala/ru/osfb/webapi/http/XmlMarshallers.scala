package ru.osfb.webapi.http

import java.io.{StringWriter, ByteArrayOutputStream, ByteArrayInputStream}

import akka.http.scaladsl.marshalling._
import akka.http.scaladsl.model.{MediaTypes, ContentType, ContentTypes, HttpCharsets}
import akka.http.scaladsl.model.MediaTypes._
import akka.http.scaladsl.unmarshalling._
import akka.stream.Materializer

import scala.xml.{MinimizeMode, Node, XML, NodeSeq}

/**
 * Created by sgl on 11.10.15.
 */
object XmlMarshallers {

  implicit def nodeSeqUnmarshaller(implicit mat: Materializer): FromEntityUnmarshaller[Node] = {
    Unmarshaller.byteArrayUnmarshaller.forContentTypes(`application/xml`, `text/xml`).map { data ⇒
      XML.load(new ByteArrayInputStream(data))
    }
  }

  implicit def nodeSeqMarshaller: ToEntityMarshaller[Node] = {
    Marshaller.StringMarshaller.wrap(MediaTypes.`application/xml`) { node =>
      val sw = new StringWriter()
      XML.write(sw, node, "UTF-8", xmlDecl = false, null, MinimizeMode.Always)
      sw.toString
    }
  }

}
