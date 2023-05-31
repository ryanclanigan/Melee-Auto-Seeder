package com.lanigan

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import com.lanigan.plugins.*

var seederTokenOverride: String? = null

fun main(args: Array<String>) {
  if (args.size == 2 && (args[0] == "--token" || args[0] == "-t")) {
    seederTokenOverride = args[1]
  }
  embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
    .start(wait = true)
}

fun Application.module() {
  val seederToken = seederTokenOverride
    ?: System.getenv("START_GG_SEEDER_AUTH_TOKEN")

  configureSecurity()
  configureHTTP()
  configureSerialization()
  configureRouting(seederToken)
}
