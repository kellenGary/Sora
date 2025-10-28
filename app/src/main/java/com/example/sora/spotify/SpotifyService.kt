package com.example.sora.spotify

import android.util.Base64
import com.example.sora.BuildConfig
import com.example.sora.auth.SpotifyAuthData
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject


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

class SpotifyService {

    private val httpClient = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
    }


    suspend fun getPlaylists(
        accessToken: String,
        refreshToken: String
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
                val newTokens = refreshSpotifyAccessToken(refreshToken)

                // retry with new token
                val retryResponse = httpClient.get("https://api.spotify.com/v1/me/playlists") {
                    header("Authorization", "Bearer ${newTokens.accessToken}")
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

    suspend fun refreshSpotifyAccessToken(refreshToken: String): SpotifyAuthData {
        val clientId = BuildConfig.SPOTIFY_CLIENT_ID
        val clientSecret = BuildConfig.SPOTIFY_CLIENT_SECRET

        // Step 1: Encode credentials as Basic Auth
        val credentials = "$clientId:$clientSecret"
        val basicAuth = Base64.encodeToString(credentials.toByteArray(), Base64.NO_WRAP)

        // Step 2: Send form request
        val response: HttpResponse = withContext(Dispatchers.IO) {
            httpClient.submitForm(
                url = "https://accounts.spotify.com/api/token",
                formParameters = Parameters.build {
                    append("grant_type", "refresh_token")
                    append("refresh_token", refreshToken)
                }
            ) {
                headers {
                    append(HttpHeaders.Authorization, "Basic $basicAuth")
                    append(
                        HttpHeaders.ContentType,
                        ContentType.Application.FormUrlEncoded.toString()
                    )
                }
            }
        }

        // Step 3: Debug output
        val bodyString = response.bodyAsText()
        println("=== Spotify Refresh Response ===")
        println(bodyString)

        if (!response.status.isSuccess()) {
            throw Exception("Failed to refresh Spotify token: $bodyString")
        }

        // Step 4: Parse new token info
        val json = Json.parseToJsonElement(bodyString).jsonObject
        val newAccessToken = json["access_token"]?.toString()?.trim('"')
            ?: throw Exception("No access_token in refresh response")
        val newExpiresIn = json["expires_in"]?.toString()?.toLongOrNull() ?: 3600
        val newRefreshToken = json["refresh_token"]?.toString()?.trim('"') ?: refreshToken

        return SpotifyAuthData(
            accessToken = newAccessToken,
            refreshToken = newRefreshToken,
            expiresIn = newExpiresIn
        )
    }
}