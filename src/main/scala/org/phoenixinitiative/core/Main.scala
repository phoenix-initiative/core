package org.phoenix.core

import org.phoenix.core.http.WebServer

object Main extends App {
  println("Starting webserver")
  WebServer.start()
}
