package com.example.sora.library.playlists

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.sora.auth.SpotifyTokenRefresher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// --- Data classes for the UI layer ---
data class PlaylistDetailsUiState(
    val isLoading: Boolean = false,
    val playlist: PlaylistDetailsResponse? = null,
    val error: String? = null
)

class PlaylistViewModel(application: Application) : AndroidViewModel(application) {
    private val spotifyService = SpotifyPlaylistManager()


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
                val accessToken = SpotifyTokenRefresher.getValidAccessToken(getApplication())
                if (accessToken == null) {
                    _uiState.value = _uiState.value.copy(
                        error = "Not connected to Spotify",
                        isLoading = false
                    )
                    return@launch
                }
                
                val playlistDetails = spotifyService.getPlaylistDetails(accessToken, playlistId)

                _uiState.value = _uiState.value.copy(
                    playlist = playlistDetails,
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
}
