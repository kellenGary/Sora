package com.example.sora.data.repository

import androidx.compose.runtime.currentRecomposeScope
import com.example.sora.auth.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.json.JsonObject

class FollowRepository {
    private val client = SupabaseClient.supabase

    suspend fun isFollowing(userId: String): Boolean {
        val currentUser = client.auth.currentUserOrNull()?: return false

        val response = client.postgrest["follow_user"]
            .select {
                filter {
                    eq("follower_id", currentUser.id)
                    eq("followee_id", userId)
                }
            }

        val rows = response.decodeList<JsonObject>()
        return rows.isNotEmpty()
    }

    suspend fun followUser(userId: String) {
        val currentUser = client.auth.currentUserOrNull()
        if (currentUser != null && currentUser.id != userId && !isFollowing(userId)) {
            client.postgrest["follow_user"]
                .insert(
                    mapOf(
                        "follower_id" to currentUser.id,
                        "followee_id" to userId
                    )
                )
        }
    }



    suspend fun unfollowUser(userId: String) {
        val currentUser = client.auth.currentUserOrNull()
        if (currentUser != null && isFollowing(userId)) {
            client.postgrest["follow_user"]
                .delete {
                    filter {
                        eq("follower_id", currentUser.id)
                        eq("followee_id", userId)
                    }
                }
        }
    }
}