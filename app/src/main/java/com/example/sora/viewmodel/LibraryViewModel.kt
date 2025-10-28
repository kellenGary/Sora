package com.example.sora.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sora.auth.AuthRepository
import com.example.sora.spotify.PlaylistItem
import com.example.sora.spotify.SpotifyService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LibraryUiState(
    val playlists: List<PlaylistItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class LibraryViewModel : ViewModel() {
    private val spotifyService = SpotifyService()
    private val authRepository = AuthRepository()

    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    fun getPlaylists(accessToken: String, refreshToken: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                val playlists = spotifyService.getPlaylists(accessToken, refreshToken)
                _uiState.value = _uiState.value.copy(
                    playlists = playlists.items,
                    isLoading = false,
                    error = null
                )

            } catch (e: Exception) {
                val errorMsg = e.message ?: ""
                println("Spotify playlist fetch failed: $errorMsg")

                if (errorMsg.contains("expired", ignoreCase = true) ||
                    errorMsg.contains("401", ignoreCase = true)
                ) {
                    try {
                        println("Refreshing Spotify token...")
                        val newTokens = spotifyService.refreshSpotifyAccessToken(refreshToken)

                        // persist the new tokens
                        authRepository.updateSpotifyCredentials(newTokens)

                        // retry playlist fetch
                        val refreshedPlaylists = spotifyService.getPlaylists(newTokens.accessToken, newTokens.refreshToken)
                        _uiState.value = _uiState.value.copy(
                            playlists = refreshedPlaylists.items,
                            isLoading = false,
                            error = null
                        )
                        println("Spotify token refreshed successfully.")
                    } catch (refreshError: Exception) {
                        println("Failed to refresh Spotify token: ${refreshError.message}")
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