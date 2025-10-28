package com.example.sora.auth

import com.example.sora.auth.SupabaseClient.supabase
import com.example.sora.data.model.Profile
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.gotrue.user.UserInfo
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put

class AuthRepository {
    private val client = SupabaseClient.supabase

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

            val user = client.auth.signUpWith(Email) {
                this.email = email
                this.password = password
                this.data = newData
            }
            if (user != null) {
                val profile = Profile(
                    id = user.id,
                    displayName = email.substringBefore('@'),
                )

                client.postgrest["profiles"].insert(profile);
            }
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

    suspend fun updateSpotifyCredentials(spotifyData: SpotifyAuthData) : Result<Unit> {
        return try {
            val user = client.auth.currentUserOrNull()
            if (user != null) {
                client.auth.updateUser {
                    data = buildJsonObject {
                        put("spotify_connected", true)
                        put("spotify_access_token", spotifyData.accessToken)
                        put("spotify_refresh_token", spotifyData.refreshToken)
                        put("spotify_expires_in", spotifyData.expiresIn)
                    }
                }
                Result.success(Unit)
            } else {
                Result.failure(Exception("No authenticated user found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun isUserLoggedIn(): Flow<Boolean> = flow {
        emit(client.auth.currentUserOrNull() != null)
    }

    fun getCurrentUser(): UserInfo {
        val session = client.auth.currentSessionOrNull()
        val user = session?.user ?: throw Exception("No authenticated user found")
        return user
    }

}
