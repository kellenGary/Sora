package com.example.sora.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.jan.supabase.gotrue.user.UserInfo
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

private const val TAG = "AuthViewModel"

class AuthViewModel : ViewModel(), IAuthViewModel {
    private val authRepository = AuthRepository()

    private val _uiState = MutableStateFlow(AuthUiState())
    override val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

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

                    val user = authRepository.getCurrentUser()
                    refreshSpotifyStatus(user)
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Login failed"
                    )
                }
        }
    }

    override fun changePassword(password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            authRepository.changePassword(password)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Password changed successfully!"
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Failed to change password"
                    )
                }
        }
    }

    override fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
                .onSuccess {
                    _uiState.value = _uiState.value.copy(isLoggedIn = false)
                }
        }
    }

    override fun clearMessages() {
        _uiState.value = _uiState.value.copy(errorMessage = null, successMessage = null)
    }

    override fun setErrorMessage(message: String) {
        _uiState.value = _uiState.value.copy(errorMessage = message)
    }

    fun refreshSpotifyStatus(user: UserInfo?) {
        val spotifyData = SpotifyCredentialsManager.getSpotifyCredentials(user)
        val isConnected = SpotifyCredentialsManager.isSpotifyConnected(user)

        _uiState.value = _uiState.value.copy(
            spotifyAuthData = spotifyData,
            isSpotifyConnected = isConnected
        )
    }

    override fun handleSpotifyAuthResult(accessToken: String, refreshToken: String, expiresIn: Long) {
        Log.d(TAG, "==================== SPOTIFY AUTH RESULT ====================")
        Log.d(TAG, "Received in AuthViewModel:")
        Log.d(TAG, "  Access Token Length: ${accessToken.length}")
        Log.d(TAG, "  Access Token (first 30 chars): ${accessToken.take(30)}...")
        Log.d(TAG, "  Refresh Token Length: ${refreshToken.length}")
        Log.d(TAG, "  Refresh Token (first 30 chars): ${refreshToken.take(30)}...")
        Log.d(TAG, "  Expires In: $expiresIn")
        
        val spotifyData = SpotifyAuthData(accessToken, refreshToken, expiresIn)
        
        Log.d(TAG, "Creating SpotifyAuthData object...")
        Log.d(TAG, "SpotifyAuthData created: $spotifyData")
        
        _uiState.value = _uiState.value.copy(
            spotifyAuthData = spotifyData,
            isSpotifyConnected = true,
            successMessage = "Spotify connected successfully!"
        )

        viewModelScope.launch {
            val result = authRepository.updateSpotifyCredentials(spotifyData)
            result.onSuccess {
                Log.d(TAG, "Spotify credentials persisted to Supabase")
            }.onFailure { e ->
                Log.e(TAG, "Failed to save Spotify credentials: ${e.message}")
            }
        }
        
        Log.d(TAG, "UI State updated:")
        Log.d(TAG, "  isSpotifyConnected: ${_uiState.value.isSpotifyConnected}")
        Log.d(TAG, "  spotifyAuthData present: ${_uiState.value.spotifyAuthData != null}")
        Log.d(TAG, "  successMessage: ${_uiState.value.successMessage}")
        Log.d(TAG, "=============================================================")
    }

    fun clearSpotifyAuth() {
        Log.d(TAG, "Clearing Spotify authentication")
        _uiState.value = _uiState.value.copy(
            spotifyAuthData = null,
            isSpotifyConnected = false
        )
    }
}