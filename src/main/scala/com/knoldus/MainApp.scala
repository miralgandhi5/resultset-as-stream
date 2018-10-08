package com.knoldus

import java.sql.{Connection, DriverManager}

import akka.http.scaladsl.marshalling.{Marshaller, Marshalling}
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.common.{CsvEntityStreamingSupport, EntityStreamingSupport}
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
  implicit val csvStreaming: CsvEntityStreamingSupport = EntityStreamingSupport.csv()
  implicit val userAsCsv: Marshaller[User, ByteString] = Marshaller.strict[User, ByteString] { t =>
    Marshalling.WithFixedContentType(ContentTypes.`text/csv(UTF-8)`, () => {
      val id = t.id
      val name = t.name
      ByteString(List(id, name).mkString(","))
    })
  }


  def route = path("hello") {
    get {
      def getConnection: Connection = {
        val connectionUrl = "jdbc:postgresql://localhost/demo?user=postgres&password=password"
        DriverManager.getConnection(connectionUrl)
      }

      val result =
        ReactiveResultSet.executeQuery(" SELECT * FROM dbusers", getConnection)(resultSet => User(
          resultSet.getInt("id"),
          resultSet.getString("name")
        ))

      complete(result)

    }
  }

  Http().bindAndHandle(route, host, port)

}
