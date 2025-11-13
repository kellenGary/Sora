package com.example.sora.library.playlists

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
data class PlaylistDetailsResponse(
    val collaborative: Boolean,
    val description: String?,
    val external_urls: ExternalUrl?,
    val href: String,
    val id: String,
    val images: List<SpotifyImage>,
    val name: String,
    val owner: PlaylistOwner,
    val public: Boolean?,
    val snapshot_id: String,
    val tracks: PlaylistTracks,
    val type: String,
    val uri: String
)

@Serializable
data class ExternalUrl(
    val spotify: String
)

@Serializable
data class SpotifyImage(
    val url: String,
    val height: Int?,
    val width: Int?
)

@Serializable
data class PlaylistOwner(
    val external_urls: ExternalUrl?,
    val href: String?,
    val id: String?,
    val type: String?,
    val uri: String?,
    val display_name: String?
)

@Serializable
data class PlaylistTracks(
    val href: String,
    val limit: Int?,
    val next: String?,
    val offset: Int,
    val previous: String?,
    val total: Int,
    val items: List<PlaylistTrackItem>
)

@Serializable
data class PlaylistTrackItem(
    val added_at: String?,
    val added_by: AddedBy?,
    val is_local: Boolean,
    val track: Track
)

@Serializable
data class AddedBy(
    val external_urls: ExternalUrl?,
    val href: String?,
    val id: String?,
    val type: String?,
    val uri: String?
)

@Serializable
data class Track(
    val album: Album,
    val artists: List<Artist>,
    val available_markets: List<String>?,
    val disc_number: Int,
    val duration_ms: Int,
    val explicit: Boolean,
    val external_ids: ExternalIds?,
    val external_urls: ExternalUrl?,
    val href: String?,
    val id: String?,
    val is_playable: Boolean? = null,
    val name: String,
    val popularity: Int?,
    val preview_url: String?,
    val track_number: Int,
    val type: String,
    val uri: String,
    val is_local: Boolean
)

@Serializable
data class ExternalIds(
    val isrc: String? = null,
    val ean: String? = null,
    val upc: String? = null
)

@Serializable
data class Album(
    val album_type: String?,
    val total_tracks: Int?,
    val available_markets: List<String>?,
    val external_urls: ExternalUrl?,
    val href: String?,
    val id: String?,
    val images: List<SpotifyImage>?,
    val name: String?,
    val release_date: String?,
    val release_date_precision: String?,
    val type: String?,
    val uri: String?,
    val artists: List<Artist>?
)

@Serializable
data class Artist(
    val external_urls: ExternalUrl?,
    val href: String?,
    val id: String?,
    val name: String?,
    val type: String?,
    val uri: String?
)


class SpotifyPlaylistManager {
    private val httpClient = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
    }


    suspend fun getPlaylistDetails(
        accessToken: String?,
        playlistId: String?
    ): PlaylistDetailsResponse {
        try {
            val response = httpClient.get("https://api.spotify.com/v1/playlists/$playlistId") {
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
                val retryResponse = httpClient.get("https://api.spotify.com/v1/playlists/$playlistId") {
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
