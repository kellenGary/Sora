package com.example.sora.feed

import android.util.Log
import com.example.sora.auth.SupabaseClient
import com.example.sora.data.model.FeedActivity
import com.example.sora.data.model.RawFeedActivity
import com.example.sora.data.model.User
import com.example.sora.data.repository.FriendFollow
import com.example.sora.data.repository.ListeningHistoryWithDetails
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class FeedRepository {
    private val TAG = "FeedRepository"
    private val client = SupabaseClient.supabase

    suspend fun getFriendsFeed(): Result<List<FeedActivity>> = withContext(Dispatchers.IO) {
        try {
            val currentUser = client.auth.currentUserOrNull()
                ?: return@withContext Result.failure(Exception("User not authenticated"))

            // 1. Get friends
            val friends = client.postgrest["follow_user"]
                .select {
                    filter { eq("follower_id", currentUser.id) }
                }
                .decodeList<FriendFollow>()
                .map { it.followeeId }

            if (friends.isEmpty())
                return@withContext Result.success(emptyList())

            // 2. Fetch from view
            val rawItems = client.postgrest["feed_activity"]
                .select {
                    filter { isIn("user_id", friends) }
                    order("timestamp", Order.DESCENDING)
                    limit(100)
                }
                .decodeList<RawFeedActivity>()

            // 3. Convert ISO → Long
            val items = rawItems.map { raw ->
                FeedActivity(
                    id = raw.id,
                    userId = raw.userId,
                    userName = raw.userName,
                    userAvatar = raw.userAvatar,
                    songTitle = raw.songTitle,
                    artist = raw.artist,
                    albumCover = raw.albumCover,
                    timestamp = kotlinx.datetime.Instant.parse(raw.timestamp).toEpochMilliseconds(),
                    latitude = raw.latitude,
                    longitude = raw.longitude,
                    activityType = raw.activityType
                )
            }

            Result.success(items)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
