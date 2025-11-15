package com.example.sora.data.repository

import android.util.Log
import com.example.sora.auth.SupabaseClient
import com.example.sora.data.model.Album
import com.example.sora.data.model.AlbumInsert
import com.example.sora.data.model.Artist
import com.example.sora.data.model.ArtistInsert
import com.example.sora.data.model.FullHistoryRow
import com.example.sora.data.model.ListenHistory
import com.example.sora.data.model.Song
import com.example.sora.data.model.SongInsert
import com.example.sora.playback.SpotifyTrack
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SongTrackingRepository {
    private val TAG = "SongTrackingRepository"
    private val supabase = SupabaseClient.supabase

    suspend fun trackSongListen(
        spotifyTrack: SpotifyTrack,
        latitude: Double,
        longitude: Double,
        userId: String? = null
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "========== trackSongListen called ==========")
            
            // Wait for session to finish loading if it's currently loading
            val sessionStatusFlow = supabase.auth.sessionStatus
            var sessionStatus = sessionStatusFlow.value
            
            Log.d(TAG, "Initial session status: $sessionStatus")
            
            if (sessionStatus is io.github.jan.supabase.gotrue.SessionStatus.LoadingFromStorage) {
                Log.d(TAG, "⏳ Session is loading from storage, waiting for completion...")
                var attempts = 0
                val maxAttempts = 20 // Wait up to 10 seconds
                
                while (sessionStatus is io.github.jan.supabase.gotrue.SessionStatus.LoadingFromStorage && attempts < maxAttempts) {
                    kotlinx.coroutines.delay(500)
                    sessionStatus = sessionStatusFlow.value
                    attempts++
                    Log.d(TAG, "  Waiting... attempt $attempts/$maxAttempts")
                }
                
                if (sessionStatus is io.github.jan.supabase.gotrue.SessionStatus.LoadingFromStorage) {
                    Log.w(TAG, "⚠ Session still loading after ${maxAttempts * 500}ms, proceeding anyway")
                } else {
                    Log.d(TAG, "✓ Session loading completed: $sessionStatus")
                }
            }
            
            val currentUser = supabase.auth.currentUserOrNull()
            
            Log.d(TAG, "Final session status: $sessionStatus")
            Log.d(TAG, "Current user: ${if (currentUser != null) "${currentUser.email} (${currentUser.id})" else "null"}")
            Log.d(TAG, "Provided userId: ${userId ?: "null"}")
            
            // Determine which userId to use
            val actualUserId = userId ?: currentUser?.id
            
            if (actualUserId == null) {
                Log.e(TAG, "✗ Cannot track song - no userId provided and no active session")
                return@withContext Result.failure(Exception("User not authenticated"))
            }
            
            Log.d(TAG, "Using userId: $actualUserId")
            
            // Log authentication method
            if (currentUser != null) {
                Log.d(TAG, "✓ Authenticated with active Supabase session (RLS will work)")
            } else {
                Log.w(TAG, "⚠ No active session - using userId only (requires RLS disabled or service role key)")
            }

            // 1. Get or create artist
            val artistId = getOrCreateArtist(spotifyTrack.artists.first())
            if (artistId == null) {
                return@withContext Result.failure(Exception("Failed to create/get artist"))
            }

            // 2. Get or create album
            val albumId = getOrCreateAlbum(spotifyTrack.album, artistId)
            if (albumId == null) {
                return@withContext Result.failure(Exception("Failed to create/get album"))
            }

            // 3. Get or create song
            val songId = getOrCreateSong(spotifyTrack, artistId, albumId)
            if (songId == null) {
                return@withContext Result.failure(Exception("Failed to create/get song"))
            }

            // 4. Create listen history entry
            val listenHistory = ListenHistory(
                userId = actualUserId,
                songId = songId,
                latitude = latitude,
                longitude = longitude,
                timestamp = System.currentTimeMillis()
            )

            supabase.from("listen_history").insert(listenHistory)
            Log.d(TAG, "Successfully tracked song: ${spotifyTrack.name}")

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error tracking song listen", e)
            Result.failure(e)
        }
    }

    private suspend fun getOrCreateArtist(spotifyArtist: com.example.sora.playback.SpotifyArtist): String? {
        return try {
            // Check if artist exists (must select all required fields for Artist model)
            val existingArtist = supabase.from("artists")
                .select() {
                    filter {
                        eq("link", spotifyArtist.uri)
                    }
                }
                .decodeSingleOrNull<Artist>()

            if (existingArtist != null) {
                Log.d(TAG, "Artist already exists: ${spotifyArtist.name}")
                return existingArtist.id
            }

            // Create new artist using insert model
            val newArtist = ArtistInsert(
                name = spotifyArtist.name,
                link = spotifyArtist.uri
            )
            val result = supabase.from("artists")
                .insert(newArtist) {
                    select()
                }
                .decodeSingle<Artist>()

            Log.d(TAG, "Created new artist: ${spotifyArtist.name}")
            result.id
        } catch (e: Exception) {
            Log.e(TAG, "Error getting/creating artist: ${spotifyArtist.name}", e)
            null
        }
    }

    private suspend fun getOrCreateAlbum(spotifyAlbum: com.example.sora.playback.SpotifyAlbum, artistId: String): String? {
        return try {
            // Check if album exists (must select all required fields for Album model)
            val existingAlbum = supabase.from("albums")
                .select() {
                    filter {
                        eq("link", spotifyAlbum.uri)
                    }
                }
                .decodeSingleOrNull<Album>()

            if (existingAlbum != null) {
                Log.d(TAG, "Album already exists: ${spotifyAlbum.name}")
                return existingAlbum.id
            }

            // Create new album using insert model
            val coverUrl = spotifyAlbum.images.firstOrNull()?.url
            val newAlbum = AlbumInsert(
                name = spotifyAlbum.name,
                cover = coverUrl,
                link = spotifyAlbum.uri,
                releaseDate = spotifyAlbum.releaseDate,
                artistId = artistId
            )

            val result = supabase.from("albums")
                .insert(newAlbum) {
                    select()
                }
                .decodeSingle<Album>()

            Log.d(TAG, "Created new album: ${spotifyAlbum.name}")
            result.id
        } catch (e: Exception) {
            Log.e(TAG, "Error getting/creating album: ${spotifyAlbum.name}", e)
            null
        }
    }

    private suspend fun getOrCreateSong(spotifyTrack: SpotifyTrack, artistId: String, albumId: String): String? {
        return try {
            // Check if song exists by matching title, artist_id, and album_id (must select all required fields)
            val existingSong = supabase.from("songs")
                .select() {
                    filter {
                        eq("title", spotifyTrack.name)
                        eq("artist_id", artistId)
                        eq("album_id", albumId)
                    }
                }
                .decodeSingleOrNull<Song>()

            if (existingSong != null) {
                Log.d(TAG, "Song already exists: ${spotifyTrack.name}")
                return existingSong.id
            }

            // Create new song using insert model
            val newSong = SongInsert(
                title = spotifyTrack.name,
                artistId = artistId,
                albumId = albumId
            )

            val result = supabase.from("songs")
                .insert(newSong) {
                    select()
                }
                .decodeSingle<Song>()

            Log.d(TAG, "Created new song: ${spotifyTrack.name}")
            result.id
        } catch (e: Exception) {
            Log.e(TAG, "Error getting/creating song: ${spotifyTrack.name}", e)
            null
        }
    }
}
