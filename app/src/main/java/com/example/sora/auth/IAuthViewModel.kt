package com.example.sora.auth

import kotlinx.coroutines.flow.StateFlow

interface IAuthViewModel {
    val uiState: StateFlow<AuthUiState>

    fun signOut()

    fun setErrorMessage(message: String)

    fun handleSpotifyAuthResult(accessToken: String, refreshToken: String, expiresIn: Long)

    fun changePassword(password: String)

    fun clearMessages()
}
