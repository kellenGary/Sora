package com.example.sora.data.repository

import android.util.Log
import com.example.sora.auth.SupabaseClient
import com.example.sora.data.model.FullHistoryRow
import com.google.android.gms.maps.model.LatLng
import com.example.sora.map.SongLocation
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FriendFollow(
    @SerialName("follower_id")
    val followerId: String,
    @SerialName("followee_id")
    val followeeId: String
)

class MapRepository {
    private val TAG = "MapRepository"
    private val client = SupabaseClient.supabase

    suspend fun getFriendsListeningHistory(): Result<List<SongLocation>> = withContext(Dispatchers.IO) {
        try {
            val currentUser = client.auth.currentUserOrNull()
            if (currentUser == null) {
                Log.e(TAG, "No authenticated user")
                return@withContext Result.failure(Exception("User not authenticated"))
            }

            Log.d(TAG, "Fetching friends for user: ${currentUser.id}")

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

            // 2. Fetch listening history for all friends with location data
            // Using raw query to join tables and get all necessary data
            val historyQuery = """
                listen_history!inner(
                    id,
                    user_id,
                    song_id,
                    latitude,
                    longitude,
                    timestamp,
                    songs!inner(
                        id,
                        title,
                        artists!inner(
                            id,
                            name
                        ),
                        albums!inner(
                            id,
                            name,
                            cover
                        )
                    )
                )
            """.trimIndent()

            // Get recent listening history for friends (last month)
            val oneMonthAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
            
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
                        gte("timestamp", oneMonthAgo)
                    }
                }

            val historyData = historyResponse.decodeList<ListeningHistoryWithDetails>()

            Log.d(TAG, "Found ${historyData.size} listening history entries")

            // 3. Convert to SongLocation objects
            val songLocations = historyData.mapNotNull { history ->
                try {
                    SongLocation(
                        id = history.id ?: "",
                        songTitle = history.songs.title,
                        artist = history.songs.artists.name,
                        location = LatLng(history.latitude, history.longitude),
                        radiusMeters = 100.0, // Default radius
                        timestamp = history.timestamp,
                        albumArtUrl = history.songs.albums.cover
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Error converting history entry to SongLocation", e)
                    null
                }
            }

            Log.d(TAG, "Converted to ${songLocations.size} song locations")
            Result.success(songLocations)

        } catch (e: Exception) {
            Log.e(TAG, "Error fetching friends listening history", e)
            Result.failure(e)
        }
    }
}

@Serializable
data class ListeningHistoryWithDetails(
    val id: String? = null,
    @SerialName("user_id")
    val userId: String,
    @SerialName("song_id")
    val songId: String,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long,
    val songs: SongWithDetails
)

@Serializable
data class SongWithDetails(
    val title: String,
    val artists: ArtistSimple,
    val albums: AlbumSimple
)

@Serializable
data class ArtistSimple(
    val name: String
)

@Serializable
data class AlbumSimple(
    val cover: String?
)
