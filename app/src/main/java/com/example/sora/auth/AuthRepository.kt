package com.example.sora.auth

import android.content.Context
import io.github.jan.supabase.gotrue.SessionStatus
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put
import android.util.Log

class AuthRepository(private val context: Context? = null) {
    private val client = SupabaseClient.supabase

    @Deprecated("Use signInOrSignUpWithSpotify instead - Spotify OAuth handles user creation")
    suspend fun signUp(email: String, password: String, spotifyData: SpotifyAuthData? = null): Result<Unit> {
        return try {
            val currentData = client.auth.currentUserOrNull()?.userMetadata?.jsonObject ?: buildJsonObject {}
            val newData = buildJsonObject {
                // copy existing metadata
                currentData.forEach { (key, value) ->
                    put(key, value)
                }

                if (spotifyData != null) {
                    put("spotify_connected", true)
                    put("spotify_access_token", spotifyData.accessToken)
                    put("spotify_refresh_token", spotifyData.refreshToken)
                    put("spotify_expires_in", spotifyData.expiresIn)
                } else {
                    put("spotify_connected", false)
                }
            }

            client.auth.signUpWith(Email) {
                this.email = email
                this.password = password
                this.data = newData
            }
            // No need for separate profiles table - all data stored in auth.users metadata
            Result.success(Unit)
        } catch (e: Exception) {
            val errorMessage = when {
                e.message?.contains("network", ignoreCase = true) == true ->
                    "Network connection failed. Please check your internet connection."
                e.message?.contains("timeout", ignoreCase = true) == true ->
                    "Connection timeout. Please try again."
                e.message?.contains("email", ignoreCase = true) == true ->
                    "Invalid email format or email already exists."
                e.message?.contains("password", ignoreCase = true) == true ->
                    "Password must be at least 6 characters long."
                else -> "Signup failed: ${e.message ?: "Unknown error occurred"}"
            }
            Result.failure(Exception(errorMessage))
        }
    }

    suspend fun signIn(email: String, password: String): Result<Unit> {
        return try {
            client.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun changePassword(password: String): Result<Unit> {
        return try {
            client.auth.updateUser {
                this.password = password
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signOut(): Result<Unit> {
        return try {
            client.auth.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signInOrCreateUserWithSpotify(
        spotifyEmail: String,
        spotifyId: String,
        displayName: String,
        spotifyData: SpotifyAuthData
    ): Result<Unit> {
        return try {
            // Use Spotify email to create a unique, deterministic account
            // Password is based on Spotify ID so returning users can login
            val password = "spotify_${spotifyId}_auto_generated_password"
            
            // Try to sign in first (for returning users)
            val signInResult = try {
                client.auth.signInWith(Email) {
                    this.email = spotifyEmail
                    this.password = password
                }

                // Successfully signed in - update Spotify tokens in metadata
                Log.d("AuthRepository", "Existing user signed in, updating tokens")
                updateSpotifyCredentials(spotifyData)
                true
            } catch (e: Exception) {
                Log.d("AuthRepository", "Sign in failed, will try signup: ${e.message}")
                false
            }

            // If sign in failed, create new account
            if (!signInResult) {
                Log.d("AuthRepository", "Creating new user account")
                client.auth.signUpWith(Email) {
                    this.email = spotifyEmail
                    this.password = password
                    // Store all user data in auth.users metadata - no separate profiles table needed
                    this.data = buildJsonObject {
                        put("spotify_connected", true)
                        put("spotify_access_token", spotifyData.accessToken)
                        put("spotify_refresh_token", spotifyData.refreshToken)
                        put("spotify_expires_in", spotifyData.expiresIn)
                        put("spotify_id", spotifyId)
                        put("auth_method", "spotify")
                        put("display_name", displayName)
                    }
                }
                Log.d("AuthRepository", "New user created with metadata")
            }

            // Save to local storage after authentication (with correct Supabase user ID)
            val supabaseUserId = client.auth.currentUserOrNull()?.id
            if (supabaseUserId != null) {
                context?.let {
                    val tokenManager = SpotifyTokenManager.getInstance(it)
                    tokenManager.saveTokens(
                        accessToken = spotifyData.accessToken,
                        refreshToken = spotifyData.refreshToken,
                        expiresIn = spotifyData.expiresIn,
                        userEmail = spotifyEmail,
                        userId = supabaseUserId, // Use Supabase user ID, not Spotify ID
                        supabasePassword = password
                    )
                    Log.d("AuthRepository", "Tokens saved to local storage with Supabase user ID: $supabaseUserId")
                }
            } else {
                Log.e("AuthRepository", "Failed to get Supabase user ID after authentication")
                return Result.failure(Exception("Failed to get user ID after authentication"))
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("AuthRepository", "Failed to sign in/create user: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun updateSpotifyCredentials(spotifyData: SpotifyAuthData) : Result<Unit> {
        return try {
            val user = client.auth.currentUserOrNull()
            if (user != null) {
                // Preserve existing metadata and update Spotify tokens in Supabase
                val currentMetadata = user.userMetadata?.jsonObject ?: buildJsonObject {}

                client.auth.updateUser {
                    data = buildJsonObject {
                        // Copy existing metadata
                        currentMetadata.forEach { (key, value) ->
                            if (key !in listOf("spotify_access_token", "spotify_refresh_token", "spotify_expires_in")) {
                                put(key, value)
                            }
                        }
                        // Update Spotify credentials
                        put("spotify_connected", true)
                        put("spotify_access_token", spotifyData.accessToken)
                        put("spotify_refresh_token", spotifyData.refreshToken)
                        put("spotify_expires_in", spotifyData.expiresIn)
                    }
                }
                
                // Save to local storage after updating Supabase (with correct Supabase user ID)
                context?.let {
                    val tokenManager = SpotifyTokenManager.getInstance(it)
                    val existingPassword = tokenManager.getSupabasePassword()
                    tokenManager.saveTokens(
                        accessToken = spotifyData.accessToken,
                        refreshToken = spotifyData.refreshToken,
                        expiresIn = spotifyData.expiresIn,
                        userEmail = user.email,
                        userId = user.id, // Use Supabase user ID
                        supabasePassword = existingPassword
                    )
                    Log.d("AuthRepository", "Tokens saved to local storage with Supabase user ID: ${user.id}")
                }
                
                Result.success(Unit)
            } else {
                Result.failure(Exception("No authenticated user found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Removed - Spotify OAuth is handled by SpotifyAuthManager directly
    // This method was trying to use Supabase's OAuth which requires
    // Spotify to be configured in Supabase dashboard, causing INVALID_CLIENT_ID error

    fun isUserLoggedIn(): Flow<Boolean> = flow {
        emit(client.auth.currentUserOrNull() != null)
    }

    fun getCurrentUser() = client.auth.currentUserOrNull()

    fun observeAuthState(): Flow<SessionStatus> = client.auth.sessionStatus

    // Helper methods to access user data from auth.users metadata
    fun getDisplayName(): String? {
        return client.auth.currentUserOrNull()?.userMetadata?.get("display_name")?.toString()?.trim('"')
    }

    fun getSpotifyId(): String? {
        return client.auth.currentUserOrNull()?.userMetadata?.get("spotify_id")?.toString()?.trim('"')
    }

    fun isSpotifyConnected(): Boolean {
        return client.auth.currentUserOrNull()?.userMetadata?.get("spotify_connected")?.toString() == "true"
    }

    fun getSpotifyAccessToken(): String? {
        // Try local storage first (works in background)
        context?.let {
            val tokenManager = SpotifyTokenManager.getInstance(it)
            val localToken = tokenManager.getAccessToken()
            if (localToken != null) {
                return localToken
            }
        }
        
        // Fallback to Supabase metadata
        return client.auth.currentUserOrNull()?.userMetadata?.get("spotify_access_token")?.toString()?.trim('"')
    }

    fun getSpotifyRefreshToken(): String? {
        // Try local storage first (works in background)
        context?.let {
            val tokenManager = SpotifyTokenManager.getInstance(it)
            val localToken = tokenManager.getRefreshToken()
            if (localToken != null) {
                return localToken
            }
        }
        
        // Fallback to Supabase metadata
        return client.auth.currentUserOrNull()?.userMetadata?.get("spotify_refresh_token")?.toString()?.trim('"')
    }
}
