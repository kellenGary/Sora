package com.example.sora.data.repository

import com.example.sora.auth.SupabaseClient
import com.example.sora.data.model.FullHistoryRow
import com.example.sora.data.model.UniqueSongCount
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order

class UserStatsRepository {
    private val client = SupabaseClient.supabase

    suspend fun getFullListeningHistory(
        userId: String,
        limit: Long = 10
    ): List<FullHistoryRow> {

        return client.postgrest["full_listening_history"]
            .select {
                filter { eq("user_id", userId) }
                order(
                    column = "timestamp",
                    order = Order.DESCENDING
                )
                limit(limit)
            }
            .decodeList<FullHistoryRow>()
    }

    suspend fun getUniqueSongCount(userId: String): Int {
        val result = client.postgrest["user_unique_song_count"]
            .select {
                filter { eq("user_id", userId) }
            }
            .decodeSingleOrNull<UniqueSongCount>()

        return result?.uniqueSongCount ?: 0
    }


}