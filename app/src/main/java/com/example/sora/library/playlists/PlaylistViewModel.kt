package com.example.sora.library.playlists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sora.auth.AuthRepository
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

class PlaylistViewModel : ViewModel() {
    private val spotifyService = SpotifyPlaylistManager()
    private val authRepository = AuthRepository()


    private val _uiState = MutableStateFlow(PlaylistDetailsUiState())
    val uiState: StateFlow<PlaylistDetailsUiState> = _uiState.asStateFlow()


    /**
     * Fetch detailed info for one playlist by its ID.
     */
    fun loadPlaylistDetails(playlistId: String?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                val accessToken = authRepository.getSpotifyAccessToken()
                val playlistDetails = spotifyService.getPlaylistDetails(accessToken, playlistId)
                System.out.println("id" + playlistDetails.id)

                _uiState.value = _uiState.value.copy(
                    playlist = playlistDetails,
                    isLoading = false,
                    error = null
                )
            } catch (e: Exception) {
                val errorMsg = e.message ?: "Unknown error"
                println("Failed to load playlist details: $errorMsg")

                // Handle expired token case
                if (errorMsg.contains("expired", ignoreCase = true) ||
                    errorMsg.contains("401", ignoreCase = true)
                ) {
                    try {
                        println("Refreshing Spotify token…")
                        val newAccessToken = authRepository.getSpotifyAccessToken()
                        val refreshedDetails = spotifyService.getPlaylistDetails(newAccessToken, playlistId)

                        _uiState.value = _uiState.value.copy(
                            playlist = refreshedDetails,
                            isLoading = false,
                            error = null
                        )
                    } catch (refreshError: Exception) {
                        println("Token refresh failed: ${refreshError.message}")
                        _uiState.value = _uiState.value.copy(
                            error = "Session expired. Please reconnect Spotify.",
                            isLoading = false
                        )
                    }
                } else {
                    _uiState.value = _uiState.value.copy(
                        error = errorMsg,
                        isLoading = false
                    )
                }
            }
        }
    }
}
