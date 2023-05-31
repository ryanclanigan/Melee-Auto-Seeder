package com.lanigan.plugins

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.http.HttpHeader
import com.lanigan.Seeder
import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import kotlinx.serialization.Serializable

fun Application.configureRouting(authToken: String) {
  val seeder = Seeder(
    ApolloClient.Builder()
      .serverUrl("https://api.start.gg/gql/alpha")
      .httpHeaders(listOf(HttpHeader("Authorization", "Bearer $authToken")))
      .build()
  )

  routing {
    get("/") {
      call.respondText("Up!")
    }

    post("/") {
      val info = call.receive<TourneyInfo>()
      seeder.fetch(info.slug)
      call.respondText("Seeded")
    }
  }
}

@Serializable
data class TourneyInfo(val slug: String)
