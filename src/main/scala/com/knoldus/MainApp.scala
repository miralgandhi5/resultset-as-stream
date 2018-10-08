package com.knoldus

import java.sql.{Connection, DriverManager}

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.util.ByteString

import scala.concurrent.{ExecutionContext, Future}

object MainApp extends App {

  val host = "0.0.0.0"
  val port = 9000

  implicit val system: ActorSystem = ActorSystem("resultset-stream")
  implicit val executor: ExecutionContext = system.dispatcher
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  def route = path("hello") {
    get {
      def getConnection: Connection = {
        val connectionUrl = "jdbc:postgresql://localhost/test?user=postgres&password=password"
        DriverManager.getConnection(connectionUrl)
      }

      val result = Future {
        ReactiveResultSet.executeQuery(" SELECT * FROM users", getConnection)(resultSet => resultSet.getInt("id"))
      }
      onSuccess(result) {
        case data => complete(HttpEntity(ContentTypes.`text/csv(UTF-8)`, data.map(ByteString(_))))
      }
    }
  }

  Http().bindAndHandle(route, host, port)



}
