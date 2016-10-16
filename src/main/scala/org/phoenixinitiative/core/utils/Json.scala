package org.phoenix.core.utils

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json._

import java.util.UUID
import java.sql.Timestamp
import java.time.Instant
import org.phoenix.core.repositories.{User, Post}

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit object UUIDJsonFormat extends RootJsonFormat[UUID] {
    def write(uuid: UUID) = JsString(uuid.toString())

    def read(value: JsValue) = value match {
      case JsString(uuidToken) => UUID.fromString(uuidToken)
      case _ => deserializationError("UUID expected")
    }
  }
  implicit object TimestampJsonFormat extends RootJsonFormat[Timestamp] {
    def write(timestamp: Timestamp) = JsString(timestamp.toInstant().toString())

    def read(value: JsValue) = value match {
      case JsString(timestampToken) => Timestamp.from(Instant.parse(timestampToken))
      case _ => deserializationError("UUID expected")
    }
  }
 

  implicit val userFormat = jsonFormat8(User)
  implicit val postFormat = jsonFormat10(Post)

}
