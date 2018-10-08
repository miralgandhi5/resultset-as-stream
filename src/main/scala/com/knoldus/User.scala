package com.knoldus

import play.api.libs.json.{Json, OFormat}

case class User(id: Int, name: String)

object User{
  implicit val userFormat: OFormat[User] = Json.format[User]
}
