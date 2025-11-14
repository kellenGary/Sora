package com.example.sora.data.repository

import com.example.sora.auth.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest

class FollowRepository {
    private val client = SupabaseClient.supabase

    suspend fun followUser(userId: String) {
        val currentUser = client.auth.currentUserOrNull()
        if (currentUser != null) {
            client.postgrest["follow_user"]
                .insert(
                    mapOf(
                        "follower_id" to currentUser.id,
                        "following_id" to userId
                    )
                )
        }
    }

    suspend fun isFollowing(userId: String): Boolean {
        val currentUser = client.auth.currentUserOrNull()?: return false

        val response = client.postgrest["follow_user"]
            .select {
                filter {
                    eq("follower_id", currentUser.id)
                    eq("following_id", userId)
                }
            }

        return response.decodeList<Map<String, Any>>().isNotEmpty()
    }

    suspend fun unfollowUser(userId: String) {
        val currentUser = client.auth.currentUserOrNull()
        if (currentUser != null && isFollowing(userId)) {
            client.postgrest["follow_user"]
                .delete {
                    filter {
                        eq("follower_id", currentUser.id)
                        eq("following_id", userId)
                    }
                }
        }
    }
}