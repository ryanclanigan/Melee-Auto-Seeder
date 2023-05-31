package com.lanigan

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.Optional
import com.lanigan.type.EventEntrantPageQuery
import com.lanigan.type.UpdatePhaseSeedInfo
import kotlinx.coroutines.runBlocking
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

private val BRAACKET_LINK = "https://braacket.com/league/comelee/ranking/53CAC125-34A8-4E95-9D51-59FD15D06052?rows=200"

fun getCurrentRankingsByPlayerName(): List<String> {
  val client = HttpClient.newBuilder().build();
  val request = HttpRequest.newBuilder()
    .uri(URI.create(BRAACKET_LINK))
    .build()
  // This contains a whole lot of nonsense, but is pretty easy to parse
  return client.send(request, HttpResponse.BodyHandlers.ofString()).body()
    .lines()
    .filter { it.contains("<a href='/league/comelee/player/") }
    .map { it.substringAfter("'>").substringBefore("<").lowercase() }
}

class Seeder(private val apolloClient: ApolloClient) {
  fun fetch(slug: String) {
    val events = runBlocking {
      apolloClient
        .query(
          PlayersQuery(
            slug, EventEntrantPageQuery(
              page = Optional.present(0),
              perPage = Optional.present(200),
            )
          )
        )
        .execute()
        .let {
          it.data ?: throw Exception("Errors: ${it.errors?.joinToString { it.message } ?: " no error messages found"}")
        }
    }.tournament!!.events!!

    val singles = events.find { it?.name?.lowercase() == "singles" }
      ?: events.find {
        it?.name?.lowercase()?.contains("melee") ?: false && it?.name?.lowercase()?.contains("singles") ?: false
      }
      ?: throw Exception("No event found with name 'Singles' or 'Melee Singles")

    if (singles.phases!!.size != 1) {
      throw Exception("Singles event must have exactly one phase")
    }

    val phase = singles.phases.first()!!.id!!

    val rankings = getCurrentRankingsByPlayerName()

    val (inRankings, unranked) = singles.entrants!!.nodes!!.partition {
      rankings.contains(
        it?.name?.lowercase()
      )
    }
    val ranked = inRankings.sortedBy { rankings.indexOf(it!!.name!!.lowercase()) } + unranked

    // TODO: This assumes each person only enters one event, might not be true
    val input = ranked.mapIndexed { index, player ->
      UpdatePhaseSeedInfo(player!!.seeds!!.find { it!!.phase!!.id!! == phase }!!.id!!, (index + 1).toString())
    }
    val response = runBlocking {
      apolloClient.mutation(
        UpdateSeedingMutation(
          phase,
          input,
        )
      ).execute()
    }

    if (response.errors != null) {
      throw Exception("Errors encountered while seeding: ${response.errors!!.joinToString(",") { it.message }}")
    }
  }
}