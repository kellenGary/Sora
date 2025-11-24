package com.example.sora.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sora.data.repository.UserRepository
import io.github.jan.supabase.gotrue.SessionStatus
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

class AuthViewModel(application: android.app.Application) : androidx.lifecycle.AndroidViewModel(application), IAuthViewModel {
    private val authRepository = AuthRepository(application)

    private val _uiState = MutableStateFlow(AuthUiState())
    override val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()
    
    // Flag to prevent race conditions between Supabase auth updates and local token saving during login
    private var isLoginInProgress = false

    init {
        val initialLoggedIn = authRepository.getCurrentUser() != null
        
        // Check for stored Spotify tokens
        val tokenManager = SpotifyTokenManager.getInstance(application)
        
        Log.d(TAG, "Initializing AuthViewModel - initialLoggedIn: $initialLoggedIn")
        
        // If we have a user, start in loading state and let the collector handle verification
        if (initialLoggedIn) {
            _uiState.value = AuthUiState(isLoading = true, isLoggedIn = false)
        } else {
            _uiState.value = AuthUiState(isLoggedIn = false)
        }

        viewModelScope.launch {
            authRepository.observeAuthState().collect { status ->
                // Skip state updates if we are in the middle of a manual login flow
                // This prevents the observer from seeing "Authenticated" before tokens are saved locally
                if (isLoginInProgress) {
                    Log.d(TAG, "Ignoring auth state change during login process")
                    return@collect
                }

                val isAuthenticated = status is SessionStatus.Authenticated
                
                // If authenticated, check for valid Spotify session
                if (isAuthenticated) {
                    // Check if user is supposed to be connected to Spotify (based on Supabase metadata)
                    val shouldHaveSpotify = authRepository.isSpotifyConnected()
                    val hasValidTokens = tokenManager.hasValidTokens()
                    
                    if (shouldHaveSpotify && !hasValidTokens) {
                        Log.d(TAG, "User is authenticated but Spotify token is invalid/expired. Attempting refresh...")
                        _uiState.value = _uiState.value.copy(isLoading = true)
                        
                        if (tokenManager.hasStoredCredentials()) {
                            val refreshResult = SpotifyTokenRefresher.refreshAccessToken(application)
                            
                            if (refreshResult.isFailure) {
                                Log.e(TAG, "Failed to refresh Spotify token. Signing out.")
                                authRepository.signOut()
                                _uiState.value = _uiState.value.copy(
                                    isLoading = false,
                                    errorMessage = "Spotify session expired. Please login again."
                                )
                                return@collect
                            } else {
                                Log.d(TAG, "Spotify token refreshed successfully.")
                            }
                        } else {
                            Log.e(TAG, "User should have Spotify but no local credentials. Signing out.")
                            authRepository.signOut()
                            return@collect
                        }
                    }
                }

                val isSpotifyConnected = tokenManager.hasStoredCredentials() && isAuthenticated
                
                _uiState.value = _uiState.value.copy(
                    isLoggedIn = isAuthenticated,
                    isSpotifyConnected = isSpotifyConnected,
                    isLoading = false
                )
                
                Log.d(TAG, "Auth state changed - isLoggedIn: $isAuthenticated, isSpotifyConnected: $isSpotifyConnected")
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

                    // Set user active
                    UserRepository().updateUserActiveStatus(true)

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
            // Set user inactive before signing out
            UserRepository().updateUserActiveStatus(false)
            
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

    fun setSpotifyLoading(isLoading: Boolean) {
        _uiState.value = _uiState.value.copy(isLoading = isLoading)
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
        Log.d(TAG, "Received tokens - creating/logging in Supabase user")

        isLoginInProgress = true
        val spotifyData = SpotifyAuthData(accessToken, refreshToken, expiresIn)

        _uiState.value = _uiState.value.copy(
            spotifyAuthData = spotifyData,
            isSpotifyConnected = true,
            isLoading = true
        )

        viewModelScope.launch {
            try {
                // Fetch Spotify user profile
                Log.d(TAG, "Fetching Spotify user profile...")
                val spotifyUser = fetchSpotifyUserProfile(accessToken)

                if (spotifyUser != null) {
                    Log.d(TAG, "Spotify user: ${spotifyUser.displayName} (${spotifyUser.email})")

                    // Create or login to Supabase
                    val result = authRepository.signInOrCreateUserWithSpotify(
                        spotifyUser.email,
                        spotifyUser.id,
                        spotifyUser.displayName,
                        spotifyData
                    )

                    result.onSuccess {
                        Log.d(TAG, "User logged in successfully")
                        
                        // Set user active
                        UserRepository().updateUserActiveStatus(true)
                        
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            isLoggedIn = true,
                            isSpotifyConnected = true,
                            successMessage = "Logged in with Spotify!"
                        )
                    }.onFailure { e ->
                        Log.e(TAG, "Failed to login: ${e.message}")
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "Login failed: ${e.message}"
                        )
                        authRepository.signOut() // Clean up if failed
                    }
                } else {
                    Log.e(TAG, "Failed to fetch Spotify profile")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Failed to fetch Spotify profile"
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Login failed: ${e.message}"
                )
            } finally {
                isLoginInProgress = false
            }
        }

        Log.d(TAG, "=============================================================")
    }

    private suspend fun fetchSpotifyUserProfile(accessToken: String): SpotifyUserProfile? = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        try {
            val url = "https://api.spotify.com/v1/me"
            val connection = java.net.URL(url).openConnection() as java.net.HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("Authorization", "Bearer $accessToken")
            connection.connect()

            if (connection.responseCode == 200) {
                val response = connection.inputStream.bufferedReader().readText()
                Log.d(TAG, "Spotify profile response: $response")

                // Parse JSON manually
                val emailMatch = Regex(""""email"\s*:\s*"([^"]+)"""").find(response)
                val displayNameMatch = Regex(""""display_name"\s*:\s*"([^"]+)"""").find(response)
                val idMatch = Regex(""""id"\s*:\s*"([^"]+)"""").find(response)

                val email = emailMatch?.groupValues?.get(1)
                val displayName = displayNameMatch?.groupValues?.get(1)
                val id = idMatch?.groupValues?.get(1)

                if (email != null && displayName != null && id != null) {
                    SpotifyUserProfile(id, email, displayName)
                } else {
                    Log.e(TAG, "Failed to parse Spotify profile")
                    null
                }
            } else {
                Log.e(TAG, "Spotify API error: ${connection.responseCode}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching Spotify profile: ${e.message}")
            null
        }
    }

    data class SpotifyUserProfile(
        val id: String,
        val email: String,
        val displayName: String
    )

    fun clearSpotifyAuth() {
        Log.d(TAG, "Clearing Spotify authentication")
        _uiState.value = _uiState.value.copy(
            spotifyAuthData = null,
            isSpotifyConnected = false
        )
    }

    fun handleLocalTokenRefresh(accessToken: String, refreshToken: String, expiresIn: Long) {
        Log.d(TAG, "Handling local token refresh - updating UI state")
        val spotifyData = SpotifyAuthData(accessToken, refreshToken, expiresIn)
        
        _uiState.value = _uiState.value.copy(
            spotifyAuthData = spotifyData,
            isSpotifyConnected = true,
            isLoggedIn = authRepository.getCurrentUser() != null
        )
        
        Log.d(TAG, "AuthViewModel state updated - isLoggedIn: ${_uiState.value.isLoggedIn}, isSpotifyConnected: ${_uiState.value.isSpotifyConnected}")
    }
}
