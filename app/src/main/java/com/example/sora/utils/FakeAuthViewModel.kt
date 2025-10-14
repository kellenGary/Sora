package com.example.sora.utils

import com.example.sora.auth.AuthUiState
import com.example.sora.auth.IAuthViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakeAuthViewModel : IAuthViewModel {
    private val _uiState = MutableStateFlow(AuthUiState())
    override val uiState: StateFlow<AuthUiState> = _uiState

    override fun signOut() {}

    override fun setErrorMessage(message: String) {}

    override fun handleSpotifyAuthResult(accessToken: String, refreshToken: String, expiresIn: Long) {}

    override fun changePassword(password: String) { }

    override fun clearMessages() { }
}