package com.example.sora.feed

import android.util.Log
import com.example.sora.auth.SupabaseClient
import com.example.sora.data.model.User
import com.example.sora.data.repository.FriendFollow
import com.example.sora.data.repository.ListeningHistoryWithDetails
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FeedPost(
    val id: String,
    val userId: String,
    val userName: String?,
    val userAvatar: String?,
    val songTitle: String,
    val artist: String,
    val albumCover: String?,
    val timestamp: Long,
    val latitude: Double,
    val longitude: Double
)

class FeedRepository {
    private val TAG = "FeedRepository"
    private val client = SupabaseClient.supabase

    suspend fun getFriendsFeed(): Result<List<FeedPost>> = withContext(Dispatchers.IO) {
        try {
            val currentUser = client.auth.currentUserOrNull()
            if (currentUser == null) {
                Log.e(TAG, "No authenticated user")
                return@withContext Result.failure(Exception("User not authenticated"))
            }

            Log.d(TAG, "Fetching friends feed for user: ${currentUser.id}")

            // 1. Get list of friends (users that current user follows)
            val friendsResponse = client.postgrest["follow_user"]
                .select {
                    filter {
                        eq("follower_id", currentUser.id)
                    }
                }

            val friends = friendsResponse.decodeList<FriendFollow>()
            val friendIds = friends.map { it.followeeId }

            Log.d(TAG, "Found ${friendIds.size} friends")

            if (friendIds.isEmpty()) {
                Log.d(TAG, "No friends found, returning empty list")
                return@withContext Result.success(emptyList())
            }

            // 2. Get user info for all friends
            val usersResponse = client.postgrest["users"]
                .select {
                    filter {
                        isIn("id", friendIds)
                    }
                }

            val users = usersResponse.decodeList<User>()
            val userMap = users.associateBy { it.id }

            Log.d(TAG, "Fetched ${users.size} user profiles")

            // 3. Fetch listening history for all friends (last week)
            val oneWeekAgo = System.currentTimeMillis() - (7L * 24 * 60 * 60 * 1000)
            
            val historyResponse = client.postgrest["listen_history"]
                .select(
                    columns = io.github.jan.supabase.postgrest.query.Columns.raw(
                        """
                        id,
                        user_id,
                        song_id,
                        latitude,
                        longitude,
                        timestamp,
                        songs!inner(
                            title,
                            artists!inner(name),
                            albums!inner(cover)
                        )
                        """.trimIndent()
                    )
                ) {
                    filter {
                        isIn("user_id", friendIds)
                        gte("timestamp", oneWeekAgo)
                    }
                    order("timestamp", order = io.github.jan.supabase.postgrest.query.Order.DESCENDING)
                }

            val historyData = historyResponse.decodeList<ListeningHistoryWithDetails>()

            Log.d(TAG, "Found ${historyData.size} listening history entries")

            // 4. Convert to FeedPost objects
            val feedPosts = historyData.mapNotNull { history ->
                try {
                    val user = userMap[history.userId]
                    FeedPost(
                        id = history.id ?: "",
                        userId = history.userId,
                        userName = user?.displayName,
                        userAvatar = user?.avatarUrl,
                        songTitle = history.songs.title,
                        artist = history.songs.artists.name,
                        albumCover = history.songs.albums.cover,
                        timestamp = history.timestamp,
                        latitude = history.latitude,
                        longitude = history.longitude
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Error converting history entry to FeedPost", e)
                    null
                }
            }

            Log.d(TAG, "Converted to ${feedPosts.size} feed posts")
            Result.success(feedPosts)

        } catch (e: Exception) {
            Log.e(TAG, "Error fetching friends feed", e)
            Result.failure(e)
        }
    }
}
