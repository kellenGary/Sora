package com.example.sora.library.main

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json


@Serializable
data class PlaylistResponse(
    val items: List<PlaylistItem>
)

@Serializable
data class PlaylistItem(
    val id: String,
    val name: String,
    val images: List<PlaylistImage>
)

@Serializable
data class PlaylistImage(
    val url: String
)

class SpotifyMainManager {

    private val httpClient = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
    }


    suspend fun getAllPlaylists(
        accessToken: String?
    ): PlaylistResponse {
        try {
            val response = httpClient.get("https://api.spotify.com/v1/me/playlists") {
                header("Authorization", "Bearer $accessToken")
            }

            val raw = response.bodyAsText()
            println("=== Spotify Playlist Response (${response.status}) ===")
            println(raw)

            if (!response.status.isSuccess()) {
                // if Spotify returned an error, print and handle
                println("Spotify API error: ${response.status}")
                throw Exception("Spotify API error: ${response.status.value} - $raw")
            }

            // parse into PlaylistResponse
            return response.body()
        } catch (e: Exception) {
            val msg = e.message ?: ""
            if (msg.contains("expired", ignoreCase = true) ||
                msg.contains("401", ignoreCase = true) ||
                msg.contains("403", ignoreCase = true)
            ) {
                println("Access token expired — refreshing Spotify token...")

                // retry with new token
                val retryResponse = httpClient.get("https://api.spotify.com/v1/me/playlists") {
                    header("Authorization", "Bearer accessToken")
                }

                val retryRaw = retryResponse.bodyAsText()
                println("=== Spotify Retry Playlist Response (${retryResponse.status}) ===")
                println(retryRaw)

                if (!retryResponse.status.isSuccess()) {
                    throw Exception("Spotify retry failed: ${retryResponse.status.value} - $retryRaw")
                }

                return retryResponse.body()
            } else {
                println("Spotify playlist fetch failed: ${e.message}")
                throw e
            }
        }
    }
}