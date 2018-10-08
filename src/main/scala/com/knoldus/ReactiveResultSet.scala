package com.knoldus

import java.sql.{Connection, PreparedStatement, ResultSet, Statement}

import akka.NotUsed
import akka.stream.scaladsl._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

// ExecutionContext for blocking ops


// Get the result of a JDBC query as a reactive stream with proper resource cleaning
object ReactiveResultSet {

  def executeQuery[A](query: String, getConnection: => Connection)(mapRow: ResultSet => A)
                     (implicit ec: ExecutionContext): Source[A, NotUsed] = {

    val futStream: Future[Source[A, NotUsed]] = Future {
      var conn: Connection = null
      var stmt: PreparedStatement = null
      var rs: ResultSet = null

      try {
        conn = getConnection
        stmt = conn.prepareStatement(query, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)

        // setFetchSize(Integer.MIN_VALUE) is a mysql driver specific way to force streaming results,
        // rather than pulling entire resultset into memory.
        // see http://dev.mysql.com/doc/connector-j/en/connector-j-reference-implementation-notes.html
        if (conn.getMetaData.getURL.matches("jdbc:mysql:.*")) {
          stmt.setFetchSize(Integer.MIN_VALUE)
        }

        rs = stmt.executeQuery()

        val stream: Source[A, NotUsed] = Source.unfoldAsync(()) { _ =>
          Future {
            if (rs.next()) Some(() -> mapRow(rs))
            else None
          }
        }

        stream.alsoTo(Sink.onComplete {
          case _ => Future(cleanup(rs, stmt, conn))
        })


      } catch {
        case NonFatal(failure) =>
          cleanup(rs, stmt, conn)
          Source.failed(failure)
      }
    }

    Source.fromFuture(futStream).flatMapConcat(identity)
  }

  private def cleanup(rs: ResultSet, stmt: Statement, conn: Connection): Unit = {
    try {
      if (rs != null) rs.close()
    } catch {
      case NonFatal(exception) => println(s"Exception closing resultset $exception")
    }
    try {
      if (stmt != null) stmt.close()
    } catch {
      case NonFatal(exception) => println(s"Exception closing statement $exception")
    }
    try {
      if (conn != null) conn.close()
    } catch {
      case NonFatal(exception) => println(s"Exception closing connection $exception")
    }
  }

}