package com.example.sora.data.repository

import android.util.Log
import com.example.sora.auth.SupabaseClient
import com.example.sora.data.model.LikedSongFull
import com.example.sora.ui.SongUi
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest

class LikedSongsRepository {
    private val client = SupabaseClient.supabase
    private val TAG = "LikedSongsRepository"

    suspend fun getLikedSongs(): List<SongUi> {
        val currentUser = client.auth.currentUserOrNull() ?: return emptyList()

        val response = client.postgrest["liked_songs_full"]
            .select {
                filter { eq("user_id", currentUser.id) }
            }

        val rows = response.decodeList<LikedSongFull>()
        return rows.map { row ->
            SongUi(
                id = row.song_id,
                title = row.song_title,
                artist = row.artist_name,
                albumArtUrl = row.album_cover,
                isLiked = true
            )
        }
    }

    suspend fun likeSong(songId: String): Boolean {
        val currentUser = client.auth.currentUserOrNull()

        if (currentUser == null) {
            Log.d(TAG, "User not authenticated")
            return
        }

        try {
            client.postgrest["liked_songs"]
                .insert(
                    mapOf(
                        "user_id" to currentUser.id,
                        "song_id" to songId
                    )
                )
            Log.d(TAG, "Successfully liked song $songId")
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to like song $songId", e)
            return false
        }
    }

    suspend fun unlikeSong(songId: String): Boolean {
        val currentUser = client.auth.currentUserOrNull()

        if (currentUser == null) {
            Log.d(TAG, "User not authenticated")
            return false
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
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to unlike song $songId", e)
            return false
        }
    }
}