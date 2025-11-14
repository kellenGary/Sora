package com.example.sora.playback

import android.util.Log
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

object SpotifyPlaybackManager {
    private const val TAG = "SpotifyPlaybackManager"
    private const val BASE_URL = "https://api.spotify.com/v1"

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(json)
        }
    }

    /**
     * Get the user's current playback state
     * Always runs on IO dispatcher to prevent blocking main thread
     */
    suspend fun getCurrentPlayback(accessToken: String): Result<SpotifyPlaybackState?> = withContext(Dispatchers.IO) {
        try {
            // Network call guaranteed to be on IO thread
            val response: HttpResponse = client.get("$BASE_URL/me/player") {
                header("Authorization", "Bearer $accessToken")
            } 

            when (response.status) {
                HttpStatusCode.OK -> {
                    // JSON parsing also happens on IO thread
                    val playbackState = json.decodeFromString<SpotifyPlaybackState>(response.bodyAsText())
                    Log.d(TAG, "Current playback: ${playbackState}")
                    Result.success(playbackState)
                }
                HttpStatusCode.NoContent -> {
                    Log.d(TAG, "No active playback")
                    Result.success(null)
                }
                HttpStatusCode.Unauthorized -> {
                    Log.e(TAG, "Unauthorized - token may be expired")
                    Result.failure(Exception("Unauthorized - please reconnect Spotify"))
                }
                else -> {
                    Log.e(TAG, "Failed to get playback: ${response.status}")
                    Result.failure(Exception("Failed to get playback: ${response.status}"))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting current playback", e)
            Result.failure(e)
        }
    }

    /**
     * Get the user's currently playing track (lighter endpoint)
     */
    suspend fun getCurrentlyPlaying(accessToken: String): Result<SpotifyPlaybackState?> = withContext(Dispatchers.IO) {
        try {
            val response: HttpResponse = client.get("$BASE_URL/me/player/currently-playing") {
                header("Authorization", "Bearer $accessToken")
            }

            when (response.status) {
                HttpStatusCode.OK -> {
                    val playbackState = json.decodeFromString<SpotifyPlaybackState>(response.bodyAsText())
                    Result.success(playbackState)
                }
                HttpStatusCode.NoContent -> {
                    Result.success(null)
                }
                else -> {
                    Result.failure(Exception("Failed to get currently playing: ${response.status}"))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting currently playing", e)
            Result.failure(e)
        }
    }

    /**
     * Resume playback
     * Always runs on IO dispatcher to prevent blocking main thread
     */
    suspend fun resume(accessToken: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response: HttpResponse = client.put("$BASE_URL/me/player/play") {
                header("Authorization", "Bearer $accessToken")
            }

            if (response.status == HttpStatusCode.NoContent || response.status == HttpStatusCode.OK) {
                Log.d(TAG, "Playback resumed")
                Result.success(Unit)
            } else {
                Log.e(TAG, "Failed to resume: ${response.status}")
                Result.failure(Exception("Failed to resume playback"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error resuming playback", e)
            Result.failure(e)
        }
    }

    /**
     * Pause playback
     * Always runs on IO dispatcher to prevent blocking main thread
     */
    suspend fun pause(accessToken: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response: HttpResponse = client.put("$BASE_URL/me/player/pause") {
                header("Authorization", "Bearer $accessToken")
            }

            if (response.status == HttpStatusCode.NoContent || response.status == HttpStatusCode.OK) {
                Log.d(TAG, "Playback paused")
                Result.success(Unit)
            } else {
                Log.e(TAG, "Failed to pause: ${response.status}")
                Result.failure(Exception("Failed to pause playback"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error pausing playback", e)
            Result.failure(e)
        }
    }

    /**
     * Skip to next track
     * Always runs on IO dispatcher to prevent blocking main thread
     */
    suspend fun skipToNext(accessToken: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response: HttpResponse = client.post("$BASE_URL/me/player/next") {
                header("Authorization", "Bearer $accessToken")
            }

            if (response.status == HttpStatusCode.NoContent || response.status == HttpStatusCode.OK) {
                Log.d(TAG, "Skipped to next track")
                Result.success(Unit)
            } else {
                Log.e(TAG, "Failed to skip: ${response.status}")
                Result.failure(Exception("Failed to skip to next"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error skipping to next", e)
            Result.failure(e)
        }
    }

    /**
     * Skip to previous track
     * Always runs on IO dispatcher to prevent blocking main thread
     */
    suspend fun skipToPrevious(accessToken: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response: HttpResponse = client.post("$BASE_URL/me/player/previous") {
                header("Authorization", "Bearer $accessToken")
            }

            if (response.status == HttpStatusCode.NoContent || response.status == HttpStatusCode.OK) {
                Log.d(TAG, "Skipped to previous track")
                Result.success(Unit)
            } else {
                Log.e(TAG, "Failed to skip to previous: ${response.status}")
                Result.failure(Exception("Failed to skip to previous"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error skipping to previous", e)
            Result.failure(e)
        }
    }

    /**
     * Seek to position in currently playing track
     * Always runs on IO dispatcher to prevent blocking main thread
     * @param positionMs Position in milliseconds
     */
    suspend fun seekToPosition(accessToken: String, positionMs: Long): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response: HttpResponse = client.put("$BASE_URL/me/player/seek") {
                header("Authorization", "Bearer $accessToken")
                parameter("position_ms", positionMs)
            }

            if (response.status == HttpStatusCode.NoContent || response.status == HttpStatusCode.OK) {
                Log.d(TAG, "Seeked to position: $positionMs")
                Result.success(Unit)
            } else {
                Log.e(TAG, "Failed to seek: ${response.status}")
                Result.failure(Exception("Failed to seek"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error seeking", e)
            Result.failure(e)
        }
    }

    /**
     * Set playback volume
     * @param volumePercent Volume percentage (0-100)
     */
    suspend fun setVolume(accessToken: String, volumePercent: Int): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response: HttpResponse = client.put("$BASE_URL/me/player/volume") {
                header("Authorization", "Bearer $accessToken")
                parameter("volume_percent", volumePercent.coerceIn(0, 100))
            }

            if (response.status == HttpStatusCode.NoContent || response.status == HttpStatusCode.OK) {
                Log.d(TAG, "Volume set to: $volumePercent")
                Result.success(Unit)
            } else {
                Log.e(TAG, "Failed to set volume: ${response.status}")
                Result.failure(Exception("Failed to set volume"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting volume", e)
            Result.failure(e)
        }
    }

    /**
     * Toggle shuffle
     */
    suspend fun setShuffle(accessToken: String, state: Boolean): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response: HttpResponse = client.put("$BASE_URL/me/player/shuffle") {
                header("Authorization", "Bearer $accessToken")
                parameter("state", state)
            }

            if (response.status == HttpStatusCode.NoContent || response.status == HttpStatusCode.OK) {
                Log.d(TAG, "Shuffle set to: $state")
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to set shuffle"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting shuffle", e)
            Result.failure(e)
        }
    }

    /**
     * Set repeat mode
     * @param state "track", "context", or "off"
     */
    suspend fun setRepeat(accessToken: String, state: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response: HttpResponse = client.put("$BASE_URL/me/player/repeat") {
                header("Authorization", "Bearer $accessToken")
                parameter("state", state)
            }

            if (response.status == HttpStatusCode.NoContent || response.status == HttpStatusCode.OK) {
                Log.d(TAG, "Repeat set to: $state")
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to set repeat"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting repeat", e)
            Result.failure(e)
        }
    }
}
