package com.example.sora.auth

import kotlinx.coroutines.flow.StateFlow

interface IAuthViewModel {
    // The UI state that contains all auth-related information.
    val uiState: StateFlow<AuthUiState>

    // Sign out function.
    fun signOut()

    // Function to set an error message
    fun setErrorMessage(message: String)

    // Function to handle Spotify auth result.
    fun handleSpotifyAuthResult(accessToken: String, refreshToken: String, expiresIn: Long)
}
