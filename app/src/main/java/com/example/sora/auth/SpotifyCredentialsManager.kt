package com.example.sora.auth

import io.github.jan.supabase.gotrue.user.UserInfo
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

object SpotifyCredentialsManager {

    fun getSpotifyCredentials(user: UserInfo?): SpotifyAuthData? {
        return try {
            val userMetadata = user?.userMetadata?.jsonObject

            val accessToken = userMetadata?.get("spotify_access_token")?.jsonPrimitive?.content
            val refreshToken = userMetadata?.get("spotify_refresh_token")?.jsonPrimitive?.content
            val expiresIn = userMetadata?.get("spotify_expires_in")?.jsonPrimitive?.content?.toLongOrNull()
            val isConnected = userMetadata?.get("spotify_connected")?.jsonPrimitive?.content?.toBooleanStrictOrNull()

            if (isConnected == true && accessToken != null && refreshToken != null && expiresIn != null) {
                SpotifyAuthData(accessToken, refreshToken, expiresIn)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    fun isSpotifyConnected(user: UserInfo?): Boolean {
        return try {
            val userMetadata = user?.userMetadata?.jsonObject
            val isConnected = userMetadata?.get("spotify_connected")?.jsonPrimitive?.content?.toBooleanStrictOrNull()
            isConnected == true
        } catch (e: Exception) {
            false
        }
    }
}
