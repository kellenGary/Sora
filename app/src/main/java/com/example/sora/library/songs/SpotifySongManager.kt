package com.example.sora.library.songs

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class SpotifyTrackResponse(
    val album: Album,
    val artists: List<Artist>,
    val available_markets: List<String>? = null,
    val disc_number: Int,
    val duration_ms: Int,
    val explicit: Boolean,
    val external_urls: ExternalUrl? = null,
    val href: String? = null,
    val id: String? = null,
    val is_playable: Boolean? = null,
    val linked_from: LinkedFrom? = null,
    val name: String,
    val popularity: Int? = null,
    val preview_url: String? = null,
    val track_number: Int,
    val type: String,
    val uri: String,
    val is_local: Boolean
)

@Serializable
data class LinkedFrom(
    val href: String? = null,
    val id: String? = null,
    val type: String? = null,
    val uri: String? = null
)

@Serializable
data class Album(
    val album_type: String? = null,
    val total_tracks: Int? = null,
    val available_markets: List<String>? = null,
    val external_urls: ExternalUrl? = null,
    val href: String? = null,
    val id: String? = null,
    val images: List<SpotifyImage>? = null,
    val name: String? = null,
    val release_date: String? = null,
    val release_date_precision: String? = null,
    val type: String? = null,
    val uri: String? = null,
    val artists: List<Artist>? = null
)

@Serializable
data class Artist(
    val external_urls: ExternalUrl? = null,
    val href: String? = null,
    val id: String? = null,
    val name: String? = null,
    val type: String? = null,
    val uri: String? = null
)

@Serializable
data class ExternalUrl(
    val spotify: String? = null
)

@Serializable
data class SpotifyImage(
    val url: String? = null,
    val height: Int? = null,
    val width: Int? = null
)


class SpotifySongManager {

    private val httpClient = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
    }

    suspend fun getSongDetails(
        accessToken: String?,
        songId: String?
    ): SpotifyTrackResponse {
        try {
            val response = httpClient.get("https://api.spotify.com/v1/tracks/$songId") {
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
                val retryResponse = httpClient.get("https://api.spotify.com/v1/tracks/$songId") {
                    header("Authorization", "Bearer $accessToken")
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
