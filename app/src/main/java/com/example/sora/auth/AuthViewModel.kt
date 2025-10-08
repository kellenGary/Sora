package com.example.sora.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SpotifyAuthData(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long
)

data class AuthUiState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val spotifyAuthData: SpotifyAuthData? = null,
    val isSpotifyConnected: Boolean = false
)

class AuthViewModel : ViewModel() {
    private val authRepository = AuthRepository()

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun signUp(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            val spotifyData = _uiState.value.spotifyAuthData
            authRepository.signUp(email, password, spotifyData)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Account created successfully! Please check your email to verify your account."
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Sign up failed"
                    )
                }
        }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            authRepository.signIn(email, password)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isLoggedIn = true,
                        successMessage = "Login successful!"
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Login failed"
                    )
                }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
                .onSuccess {
                    _uiState.value = _uiState.value.copy(isLoggedIn = false)
                }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(errorMessage = null, successMessage = null)
    }

    fun setErrorMessage(message: String) {
        _uiState.value = _uiState.value.copy(errorMessage = message)
    }

    fun handleSpotifyAuthResult(accessToken: String, refreshToken: String, expiresIn: Long) {
        Log.d("AuthViewModel", "==================== SPOTIFY AUTH RESULT ====================")
        Log.d("AuthViewModel", "Received in AuthViewModel:")
        Log.d("AuthViewModel", "  Access Token Length: ${accessToken.length}")
        Log.d("AuthViewModel", "  Access Token (first 30 chars): ${accessToken.take(30)}...")
        Log.d("AuthViewModel", "  Refresh Token Length: ${refreshToken.length}")
        Log.d("AuthViewModel", "  Refresh Token (first 30 chars): ${refreshToken.take(30)}...")
        Log.d("AuthViewModel", "  Expires In: $expiresIn")
        
        val spotifyData = SpotifyAuthData(accessToken, refreshToken, expiresIn)
        
        Log.d("AuthViewModel", "Creating SpotifyAuthData object...")
        Log.d("AuthViewModel", "SpotifyAuthData created: $spotifyData")
        
        _uiState.value = _uiState.value.copy(
            spotifyAuthData = spotifyData,
            isSpotifyConnected = true,
            successMessage = "Spotify connected successfully!"
        )
        
        Log.d("AuthViewModel", "UI State updated:")
        Log.d("AuthViewModel", "  isSpotifyConnected: ${_uiState.value.isSpotifyConnected}")
        Log.d("AuthViewModel", "  spotifyAuthData present: ${_uiState.value.spotifyAuthData != null}")
        Log.d("AuthViewModel", "  successMessage: ${_uiState.value.successMessage}")
        Log.d("AuthViewModel", "=============================================================")
    }

    fun clearSpotifyAuth() {
        Log.d("AuthViewModel", "Clearing Spotify authentication")
        _uiState.value = _uiState.value.copy(
            spotifyAuthData = null,
            isSpotifyConnected = false
        )
    }
}
