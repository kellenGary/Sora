package com.example.sora.library.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.sora.auth.SpotifyTokenRefresher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LibraryUiState(
    val playlists: List<PlaylistItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class LibraryViewModel(application: Application) : AndroidViewModel(application) {
    private val spotifyService = SpotifyMainManager()

    private var hasLoaded = false

    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    init {
        if (!hasLoaded) {
            hasLoaded = true
            getAllPlaylists()
        }
    }
    fun getAllPlaylists() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

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
                
                val playlists = spotifyService.getAllPlaylists(accessToken)
                _uiState.value = _uiState.value.copy(
                    playlists = playlists.items,
                    isLoading = false,
                    error = null
                )
            } catch (e: Exception) {
                println("Spotify playlist fetch failed: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to load playlists",
                    isLoading = false
                )
            }
        }
    }
}