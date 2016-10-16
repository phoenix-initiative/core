package org.phoenix.core.http

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import scala.io.StdIn

object WebServer {

  def start() {

    implicit val system = ActorSystem("phoenix-system")
    implicit val materializer = ActorMaterializer()
    // For flatmap and scala concurrency
    implicit val executionContext = system.dispatcher

    val route = path("hello") {
      get {
        complete(HttpEntity(ContentTypes.`text/plain(UTF-8)`, "Hello Phoenix"))
      }
    }

    val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)

    println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
    StdIn.readLine() // Wait for enter
    bindingFuture
      .flatMap(_.unbind()) //Unbind from port
      .onComplete(_ => system.terminate()) //Shutdown
  }

}
