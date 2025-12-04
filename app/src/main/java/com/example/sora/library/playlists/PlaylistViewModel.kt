package com.example.sora.library.playlists

import android.app.Application
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.sora.auth.SpotifyTokenRefresher
import com.example.sora.auth.SupabaseClient.supabase
import com.example.sora.data.model.SharedPlaylist
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.UUID

// --- Data classes for the UI layer ---
data class PlaylistDetailsUiState(
    val isLoading: Boolean = false,
    val playlist: PlaylistDetailsResponse? = null,
    val error: String? = null,
    val displayedTracks: List<PlaylistTrackItem> = emptyList(),
    val hasMoreTracks: Boolean = false,
    val isLoadingMore: Boolean = false
)

class PlaylistViewModel(application: Application) : AndroidViewModel(application) {
    private val spotifyService = SpotifyPlaylistManager()
    var isShared by mutableStateOf(false)
        private set

    private val tracksPerPage = 20
    private var currentPage = 0
    private var allTracks: List<PlaylistTrackItem> = emptyList()

    private val _uiState = MutableStateFlow(PlaylistDetailsUiState())
    val uiState: StateFlow<PlaylistDetailsUiState> = _uiState.asStateFlow()


    /**
     * Fetch detailed info for one playlist by its ID.
     */
    fun loadPlaylistDetails(playlistId: String?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                // Get valid token (auto-refreshes if expired)
                val accessToken = try {
                    SpotifyTokenRefresher.getValidAccessToken(getApplication())
                } catch (e: SpotifyTokenRefresher.RefreshTokenRevokedException) {
                    Log.e("PlaylistViewModel", "Refresh token revoked", e)
                    _uiState.value = _uiState.value.copy(
                        error = "Spotify connection expired. Please log in again.",
                        isLoading = false
                    )
                    return@launch
                }
                if (accessToken == null) {
                    _uiState.value = _uiState.value.copy(
                        error = "Not connected to Spotify",
                        isLoading = false
                    )
                    return@launch
                }
                
                val playlistDetails = spotifyService.getPlaylistDetails(accessToken, playlistId)

                // Store all tracks but only display first page
                allTracks = playlistDetails.tracks.items
                currentPage = 0
                val initialTracks = allTracks.take(tracksPerPage)
                
                _uiState.value = _uiState.value.copy(
                    playlist = playlistDetails,
                    displayedTracks = initialTracks,
                    hasMoreTracks = allTracks.size > tracksPerPage,
                    isLoading = false,
                    error = null
                )
            } catch (e: Exception) {
                println("Failed to load playlist details: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to load playlist",
                    isLoading = false
                )
            }
        }
    }

    /**
     * Load next page of tracks (pagination for performance)
     */
    fun loadMoreTracks() {
        if (_uiState.value.isLoadingMore || !_uiState.value.hasMoreTracks) {
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingMore = true)

            // Simulate slight delay for smooth UX (optional)
            kotlinx.coroutines.delay(100)

            currentPage++
            val startIndex = currentPage * tracksPerPage
            val endIndex = minOf(startIndex + tracksPerPage, allTracks.size)
            val newTracks = allTracks.subList(startIndex, endIndex)

            val updatedTracks = _uiState.value.displayedTracks + newTracks

            _uiState.value = _uiState.value.copy(
                displayedTracks = updatedTracks,
                hasMoreTracks = endIndex < allTracks.size,
                isLoadingMore = false
            )
        }
    }

    // 2. The simplified function (No caption needed anymore)
    @RequiresApi(Build.VERSION_CODES.O)
    fun sharePlaylistToFeed(
        playlistId: String?,
        name: String,
        imageUrl: String,
        owner: String
    ) {
        viewModelScope.launch {
            try {
                val currentUserId = supabase.auth.currentUserOrNull()?.id ?: return@launch

                val shareItem = SharedPlaylist(
                    id = UUID.randomUUID().toString(),
                    createdAt = Instant.now().toString(),
                    userId = currentUserId,
                    playlistId = playlistId,
                    playlistName = name,
                    playlistImageUrl = imageUrl,
                )

                supabase.from("shared_playlists").insert(shareItem)

                isShared = true

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
