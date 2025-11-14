package com.example.sora.auth

import android.content.Context
import android.util.Base64
import android.util.Log
import com.example.sora.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

object SpotifyTokenRefresher {
    private const val TAG = "SpotifyTokenRefresher"
    private const val TOKEN_URL = "https://accounts.spotify.com/api/token"
    
    data class TokenResponse(
        val accessToken: String,
        val expiresIn: Long,
        val refreshToken: String? = null
    )
    
    suspend fun refreshAccessToken(context: Context): Result<TokenResponse> = withContext(Dispatchers.IO) {
        try {
            val tokenManager = SpotifyTokenManager.getInstance(context)
            val refreshToken = tokenManager.getRefreshToken()
            
            if (refreshToken == null) {
                Log.e(TAG, "No refresh token available")
                return@withContext Result.failure(Exception("No refresh token available"))
            }
            
            Log.d(TAG, "Refreshing Spotify access token...")
            
            val url = URL(TOKEN_URL)
            val connection = url.openConnection() as HttpURLConnection
            
            try {
                connection.requestMethod = "POST"
                connection.doOutput = true
                
                // Create Basic Auth header
                val clientId = BuildConfig.SPOTIFY_CLIENT_ID
                val clientSecret = getSpotifyClientSecret()
                val credentials = "$clientId:$clientSecret"
                val encodedCredentials = Base64.encodeToString(
                    credentials.toByteArray(),
                    Base64.NO_WRAP
                )
                
                connection.setRequestProperty("Authorization", "Basic $encodedCredentials")
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
                
                // Build request body
                val body = "grant_type=refresh_token&refresh_token=$refreshToken"
                
                OutputStreamWriter(connection.outputStream).use { writer ->
                    writer.write(body)
                    writer.flush()
                }
                
                val responseCode = connection.responseCode
                if (responseCode == 200) {
                    val response = connection.inputStream.bufferedReader().readText()
                    val jsonResponse = JSONObject(response)
                    
                    val newAccessToken = jsonResponse.getString("access_token")
                    val expiresIn = jsonResponse.getLong("expires_in") * 1000 // Convert to milliseconds
                    val newRefreshToken = jsonResponse.optString("refresh_token", refreshToken)
                    
                    // Save updated tokens
                    tokenManager.saveTokens(
                        accessToken = newAccessToken,
                        refreshToken = newRefreshToken,
                        expiresIn = expiresIn,
                        userEmail = tokenManager.getUserEmail(),
                        userId = tokenManager.getUserId()
                    )
                    
                    // Update Supabase user metadata
                    val authRepository = AuthRepository()
                    authRepository.updateSpotifyCredentials(
                        SpotifyAuthData(newAccessToken, newRefreshToken, expiresIn)
                    )
                    
                    Log.d(TAG, "Token refreshed successfully")
                    
                    Result.success(TokenResponse(newAccessToken, expiresIn, newRefreshToken))
                } else {
                    val error = connection.errorStream?.bufferedReader()?.readText() ?: "Unknown error"
                    Log.e(TAG, "Token refresh failed: $responseCode - $error")
                    Result.failure(Exception("Token refresh failed: $error"))
                }
            } finally {
                connection.disconnect()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error refreshing token", e)
            Result.failure(e)
        }
    }
    
    suspend fun getValidAccessToken(context: Context): String? {
        val tokenManager = SpotifyTokenManager.getInstance(context)
        
        // Check if token is valid
        if (tokenManager.hasValidTokens()) {
            return tokenManager.getAccessToken()
        }
        
        // Token is expired or invalid, try to refresh
        if (tokenManager.getRefreshToken() != null) {
            Log.d(TAG, "Token expired, attempting refresh...")
            val result = refreshAccessToken(context)
            
            result.onSuccess { tokenResponse ->
                return tokenResponse.accessToken
            }.onFailure { exception ->
                Log.e(TAG, "Failed to refresh token", exception)
                return null
            }
        }
        
        return null
    }
    
    private fun getSpotifyClientSecret(): String {
        return try {
            BuildConfig.SPOTIFY_CLIENT_SECRET.takeIf { it.isNotBlank() } ?: run {
                Log.e(TAG, "SPOTIFY_CLIENT_SECRET is empty. Please add it to gradle.properties")
                ""
            }
        } catch (e: Exception) {
            Log.e(TAG, "SPOTIFY_CLIENT_SECRET not found in BuildConfig", e)
            ""
        }
    }
}
