package com.example.sora.library.songs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sora.auth.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SongDetailsUiState(
    val isLoading: Boolean = false,
    val song: SpotifyTrackResponse? = null,
    val error: String? = null
)

class SongViewModel() : ViewModel() {

    private val spotifyService = SpotifySongManager()
    private val authRepository = AuthRepository()

    private val _uiState = MutableStateFlow(SongDetailsUiState())
    val uiState: StateFlow<SongDetailsUiState> = _uiState.asStateFlow()


    fun loadSongDetails(songId: String?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                val accessToken = authRepository.getSpotifyAccessToken()
                val songDetails = spotifyService.getSongDetails(accessToken, songId)

                _uiState.value = _uiState.value.copy(
                    song = songDetails,
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
                        val refreshedDetails = spotifyService.getSongDetails(newAccessToken, songId)

                        _uiState.value = _uiState.value.copy(
                            song = refreshedDetails,
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