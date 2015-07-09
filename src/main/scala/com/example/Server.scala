package com.example

import java.net.URI
import java.sql.{Connection, DriverManager}

import com.twitter.finagle.{Httpx, Service}
import com.twitter.finagle.httpx
import com.twitter.util.{Await, Future}

import scala.util.Properties

import com.heroku.sdk.jdbc.DatabaseUrl

object Server {
  def main(args: Array[String]) {
    val port = Properties.envOrElse("PORT", "8080").toInt
    println("Starting on port: "+port)

    val service = new Hello
    val server = Httpx.serve(":" + port, service)
    Await.ready(server)
  }
}

class Hello extends Service[httpx.Request, httpx.Response] {
  def apply(request: httpx.Request): Future[httpx.Response] = {
    if (request.uri.endsWith("/db")) {
      showDatabase(request);
    } else {
      showHome(request);
    }
  }

  def showHome(request: httpx.Request): Future[httpx.Response] = {
    val response = httpx.Response()
    response.setStatusCode(200)
    response.setContentString("Hello from Scala and Docker!")
    Future(response)
  }

  def showDatabase(request: httpx.Request): Future[httpx.Response] = {
    try {
      val connection = DatabaseUrl.extract(System.getenv("STACK") == null).getConnection
      try {
        val stmt = connection.createStatement
        stmt.executeUpdate("CREATE TABLE IF NOT EXISTS ticks (tick timestamp)")
        stmt.executeUpdate("INSERT INTO ticks VALUES (now())")

        val rs = stmt.executeQuery("SELECT tick FROM ticks")

        var out = ""
        while (rs.next) {
          out += "Read from DB: " + rs.getTimestamp("tick") + "\n"
        }

        val response = httpx.Response()
        response.setStatusCode(200)
        response.setContentString(out)
        Future(response)
      } finally {
        connection.close()
      }
    } catch {
      case e: Exception => e.printStackTrace; throw e
    }
  }
}
