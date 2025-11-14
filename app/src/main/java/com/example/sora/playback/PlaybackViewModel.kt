package com.example.sora.playback

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.sora.auth.AuthRepository
import com.example.sora.auth.SpotifyTokenRefresher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PlaybackViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "PlaybackViewModel"
    private val authRepository = AuthRepository(application)
    
    private val _uiState = MutableStateFlow(PlaybackUiState())
    val uiState: StateFlow<PlaybackUiState> = _uiState.asStateFlow()
    
    private var pollingJob: Job? = null
    private var progressUpdateJob: Job? = null
    
    // Track the last known position to smoothly update UI
    private var lastKnownProgressMs: Long = 0L
    private var lastUpdateTimestamp: Long = 0L
    
    init {
        startPolling()
    }
    
    /**
     * Start polling Spotify API for playback state
     * Runs entirely in background thread with no blocking
     * Polls every 5 seconds, or faster when there are errors
     */
    fun startPolling() {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch(Dispatchers.IO) {
            while (isActive) {
                try {
                    refreshPlaybackStateBackground()
                    delay(5000) // Poll every 5 seconds
                } catch (e: Exception) {
                    Log.e(TAG, "Error in polling loop", e)
                    delay(10000) // Wait longer on error
                }
            }
        }
    }
    
    /**
     * Stop polling for playback state
     */
    fun stopPolling() {
        pollingJob?.cancel()
        progressUpdateJob?.cancel()
    }
    
    /**
     * Refresh playback state from Spotify API in background thread
     * This method runs entirely on background thread and updates UI asynchronously
     */
    private suspend fun refreshPlaybackStateBackground() {
        // Get valid token (auto-refreshes if expired)
        val accessToken = withContext(Dispatchers.IO) {
            SpotifyTokenRefresher.getValidAccessToken(getApplication())
        }
        
        if (accessToken == null) {
            Log.e(TAG, "No Spotify access token found - user needs to reconnect Spotify")
            // Update UI on main thread, but don't block
            withContext(Dispatchers.Main) {
                _uiState.value = _uiState.value.copy(
                    error = "Not connected to Spotify",
                    isLoading = false
                )
            }
            return
        }
        
        Log.d(TAG, "Polling Spotify API for current playback...")
        // Network call happens on IO dispatcher (already handled in SpotifyPlaybackManager)
        val result = SpotifyPlaybackManager.getCurrentPlayback(accessToken)
        
        result.onSuccess { playbackState ->
            if (playbackState == null) {
                // No active playback
                Log.d(TAG, "No active playback detected")
                withContext(Dispatchers.Main) {
                    _uiState.value = PlaybackUiState(
                        hasActiveDevice = false,
                        isLoading = false
                    )
                }
                progressUpdateJob?.cancel()
            } else {
                Log.d(TAG, "Active playback detected: ${playbackState.item?.name} by ${playbackState.item?.artists?.firstOrNull()?.name}")
                // Update with current playback
                val currentProgress = playbackState.progressMs ?: 0L
                val timestamp = System.currentTimeMillis()
                val isPlaying = playbackState.isPlaying
                
                // Update UI on main thread
                withContext(Dispatchers.Main) {
                    lastKnownProgressMs = currentProgress
                    lastUpdateTimestamp = timestamp
                    
                    _uiState.value = PlaybackUiState(
                        track = playbackState.item,
                        isPlaying = isPlaying,
                        progressMs = currentProgress,
                        durationMs = playbackState.item?.durationMs ?: 0L,
                        hasActiveDevice = playbackState.device != null,
                        isLoading = false
                    )
                    
                    // Start smooth progress updates if playing
                    if (isPlaying) {
                        startProgressUpdates()
                    } else {
                        progressUpdateJob?.cancel()
                    }
                }
            }
        }.onFailure { exception ->
            Log.e(TAG, "Failed to refresh playback state", exception)
            withContext(Dispatchers.Main) {
                _uiState.value = _uiState.value.copy(
                    error = exception.message,
                    isLoading = false
                )
            }
        }
    }
    
    /**
     * Refresh playback state (used by UI-triggered refreshes)
     * Launches in background and returns immediately
     */
    private fun refreshPlaybackState() {
        viewModelScope.launch(Dispatchers.IO) {
            refreshPlaybackStateBackground()
        }
    }
    
    /**
     * Update progress smoothly every second when playing
     * Runs on main thread but only does lightweight calculations
     */
    private fun startProgressUpdates() {
        progressUpdateJob?.cancel()
        progressUpdateJob = viewModelScope.launch(Dispatchers.Main) {
            while (isActive && _uiState.value.isPlaying) {
                delay(1000)
                
                // Calculate elapsed time since last update (lightweight operation)
                val now = System.currentTimeMillis()
                val elapsed = now - lastUpdateTimestamp
                val newProgress = lastKnownProgressMs + elapsed
                
                // Don't exceed duration
                val duration = _uiState.value.durationMs
                if (duration > 0 && newProgress < duration) {
                    _uiState.value = _uiState.value.copy(progressMs = newProgress)
                } else if (duration > 0 && newProgress >= duration) {
                    // Track finished, refresh state in background
                    refreshPlaybackState()
                }
            }
        }
    }
    
    /**
     * Toggle play/pause
     * Runs entirely in background, returns immediately
     */
    fun togglePlayPause() {
        // Immediately update UI for instant feedback
        val wasPlaying = _uiState.value.isPlaying
        _uiState.value = _uiState.value.copy(isLoading = true)
        
        // Do network call in background
        viewModelScope.launch(Dispatchers.IO) {
            // Get valid token (auto-refreshes if expired)
            val accessToken = SpotifyTokenRefresher.getValidAccessToken(getApplication())
            if (accessToken == null) {
                withContext(Dispatchers.Main) {
                    _uiState.value = _uiState.value.copy(
                        error = "Not connected to Spotify",
                        isLoading = false,
                        isPlaying = wasPlaying
                    )
                }
                return@launch
            }
            
            val result = if (wasPlaying) {
                SpotifyPlaybackManager.pause(accessToken)
            } else {
                SpotifyPlaybackManager.resume(accessToken)
            }
            
            result.onSuccess {
                // Update UI on main thread
                withContext(Dispatchers.Main) {
                    _uiState.value = _uiState.value.copy(
                        isPlaying = !wasPlaying,
                        isLoading = false
                    )
                    
                    if (!_uiState.value.isPlaying) {
                        // Save current progress when pausing
                        lastKnownProgressMs = _uiState.value.progressMs
                        lastUpdateTimestamp = System.currentTimeMillis()
                        progressUpdateJob?.cancel()
                    } else {
                        // Restart progress updates when playing
                        lastKnownProgressMs = _uiState.value.progressMs
                        lastUpdateTimestamp = System.currentTimeMillis()
                        startProgressUpdates()
                    }
                }
                
                // Refresh to confirm in background
                delay(500)
                refreshPlaybackStateBackground()
            }.onFailure { exception ->
                Log.e(TAG, "Failed to toggle play/pause", exception)
                withContext(Dispatchers.Main) {
                    _uiState.value = _uiState.value.copy(
                        error = exception.message,
                        isLoading = false,
                        isPlaying = wasPlaying // Revert on failure
                    )
                }
            }
        }
    }
    
    /**
     * Skip to next track
     * Runs entirely in background, returns immediately
     */
    fun skipToNext() {
        // Immediately update UI
        _uiState.value = _uiState.value.copy(isLoading = true)
        
        // Do network call in background
        viewModelScope.launch(Dispatchers.IO) {
            // Get valid token (auto-refreshes if expired)
            val accessToken = SpotifyTokenRefresher.getValidAccessToken(getApplication())
            if (accessToken == null) {
                withContext(Dispatchers.Main) {
                    _uiState.value = _uiState.value.copy(
                        error = "Not connected to Spotify",
                        isLoading = false
                    )
                }
                return@launch
            }
            
            val result = SpotifyPlaybackManager.skipToNext(accessToken)
            
            result.onSuccess {
                // Wait a moment for Spotify to switch tracks
                delay(500)
                refreshPlaybackStateBackground()
            }.onFailure { exception ->
                Log.e(TAG, "Failed to skip to next", exception)
                withContext(Dispatchers.Main) {
                    _uiState.value = _uiState.value.copy(
                        error = exception.message,
                        isLoading = false
                    )
                }
            }
        }
    }
    
    /**
     * Skip to previous track
     * Runs entirely in background, returns immediately
     */
    fun skipToPrevious() {
        // Immediately update UI
        _uiState.value = _uiState.value.copy(isLoading = true)
        
        // Do network call in background
        viewModelScope.launch(Dispatchers.IO) {
            // Get valid token (auto-refreshes if expired)
            val accessToken = SpotifyTokenRefresher.getValidAccessToken(getApplication())
            if (accessToken == null) {
                withContext(Dispatchers.Main) {
                    _uiState.value = _uiState.value.copy(
                        error = "Not connected to Spotify",
                        isLoading = false
                    )
                }
                return@launch
            }
            
            val result = SpotifyPlaybackManager.skipToPrevious(accessToken)
            
            result.onSuccess {
                delay(500)
                refreshPlaybackStateBackground()
            }.onFailure { exception ->
                Log.e(TAG, "Failed to skip to previous", exception)
                withContext(Dispatchers.Main) {
                    _uiState.value = _uiState.value.copy(
                        error = exception.message,
                        isLoading = false
                    )
                }
            }
        }
    }
    
    /**
     * Seek to position in track
     * Runs entirely in background, returns immediately
     * @param positionMs Position in milliseconds
     */
    fun seekTo(positionMs: Long) {
        // Immediately update UI for instant feedback
        lastKnownProgressMs = positionMs
        lastUpdateTimestamp = System.currentTimeMillis()
        _uiState.value = _uiState.value.copy(progressMs = positionMs)
        
        // Do network call in background
        viewModelScope.launch(Dispatchers.IO) {
            // Get valid token (auto-refreshes if expired)
            val accessToken = SpotifyTokenRefresher.getValidAccessToken(getApplication())
            if (accessToken == null) {
                withContext(Dispatchers.Main) {
                    _uiState.value = _uiState.value.copy(error = "Not connected to Spotify")
                }
                return@launch
            }
            
            val result = SpotifyPlaybackManager.seekToPosition(accessToken, positionMs)
            
            result.onSuccess {
                // Confirm with API in background
                delay(500)
                refreshPlaybackStateBackground()
            }.onFailure { exception ->
                Log.e(TAG, "Failed to seek", exception)
                withContext(Dispatchers.Main) {
                    _uiState.value = _uiState.value.copy(error = exception.message)
                }
            }
        }
    }
    
    /**
     * Manually refresh playback state (pull to refresh, etc.)
     * Returns immediately, refresh happens in background
     */
    fun refresh() {
        _uiState.value = _uiState.value.copy(isLoading = true)
        refreshPlaybackState()
    }
    
    override fun onCleared() {
        super.onCleared()
        stopPolling()
    }
}
