package com.example.sora.utils

import com.example.sora.data.model.ListenHistory
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order

suspend fun getLastListen(supabase: SupabaseClient, userId: String): ListenHistory? {
    return supabase.from("listen_history")
        .select {
            filter {
                eq("user_id", userId)
            }
            order("timestamp", Order.DESCENDING)
            limit(1)
        }
        .decodeSingleOrNull<ListenHistory>()
}