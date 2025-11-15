package com.example.sora.data.repository

import android.util.Log
import com.example.sora.auth.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest

class LikedSongsRepository {
    private val client = SupabaseClient.supabase
    private val TAG = "LikedSongsRepository"

    suspend fun getLikedSongIds(): List<String> {
        val currentUser = client.auth.currentUserOrNull() ?: return emptyList()

        val response = client.postgrest["liked_songs"]
            .select {
                filter { eq("user_id", currentUser.id) }
            }

        val rows = response.decodeList<Map<String, String>>()
        return rows.map { it["song_id"]!! }
    }

    suspend fun likeSong(songId: String) {
        val currentUser = client.auth.currentUserOrNull()

        if (currentUser == null) {
            Log.d(TAG, "User not authenticated")
            return
        }

        try {
            client.postgrest["liked_songs"]
                .insert({
                    "user_id" to currentUser.id
                    "song_id" to songId
                })
            Log.d(TAG, "Successfully liked song $songId")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to like song $songId", e)
        }

    }

    suspend fun unlikeSong(songId: String) {
        val currentUser = client.auth.currentUserOrNull()

        if (currentUser == null) {
            Log.d(TAG, "User not authenticated")
            return
        }

        try {
            client.postgrest["liked_songs"]
                .delete {
                    filter {
                        eq("user_id", currentUser.id)
                        eq("song_id", songId)
                    }
                }
            Log.d(TAG, "Successfully unliked song $songId")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to unlike song $songId", e)
        }
    }
}