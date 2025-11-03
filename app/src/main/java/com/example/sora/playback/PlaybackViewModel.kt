package com.example.sora.playback

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sora.auth.AuthRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class PlaybackViewModel : ViewModel() {
    private val TAG = "PlaybackViewModel"
    private val authRepository = AuthRepository()
    
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
     * Polls every 5 seconds, or faster when there are errors
     */
    fun startPolling() {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (isActive) {
                try {
                    refreshPlaybackState()
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
     * Refresh playback state from Spotify API
     */
    private suspend fun refreshPlaybackState() {
        val accessToken = authRepository.getSpotifyAccessToken()
        
        if (accessToken == null) {
            Log.e(TAG, "No Spotify access token found - user needs to reconnect Spotify")
            _uiState.value = _uiState.value.copy(
                error = "Not connected to Spotify",
                isLoading = false
            )
            return
        }
        
        Log.d(TAG, "Polling Spotify API for current playback...")
        val result = SpotifyPlaybackManager.getCurrentPlayback(accessToken)
        
        result.onSuccess { playbackState ->
            if (playbackState == null) {
                // No active playback
                Log.d(TAG, "No active playback detected")
                _uiState.value = PlaybackUiState(
                    hasActiveDevice = false,
                    isLoading = false
                )
                progressUpdateJob?.cancel()
            } else {
                Log.d(TAG, "Active playback detected: ${playbackState.item?.name} by ${playbackState.item?.artists?.firstOrNull()?.name}")
                // Update with current playback
                val currentProgress = playbackState.progressMs ?: 0L
                lastKnownProgressMs = currentProgress
                lastUpdateTimestamp = System.currentTimeMillis()
                
                _uiState.value = PlaybackUiState(
                    track = playbackState.item,
                    isPlaying = playbackState.isPlaying,
                    progressMs = currentProgress,
                    durationMs = playbackState.item?.durationMs ?: 0L,
                    hasActiveDevice = playbackState.device != null,
                    isLoading = false
                )
                
                // Start smooth progress updates if playing
                if (playbackState.isPlaying) {
                    startProgressUpdates()
                } else {
                    progressUpdateJob?.cancel()
                }
            }
        }.onFailure { exception ->
            Log.e(TAG, "Failed to refresh playback state", exception)
            _uiState.value = _uiState.value.copy(
                error = exception.message,
                isLoading = false
            )
        }
    }
    
    /**
     * Update progress smoothly every second when playing
     */
    private fun startProgressUpdates() {
        progressUpdateJob?.cancel()
        progressUpdateJob = viewModelScope.launch {
            while (isActive && _uiState.value.isPlaying) {
                delay(1000)
                
                // Calculate elapsed time since last update
                val now = System.currentTimeMillis()
                val elapsed = now - lastUpdateTimestamp
                val newProgress = lastKnownProgressMs + elapsed
                
                // Don't exceed duration
                val duration = _uiState.value.durationMs
                if (duration > 0 && newProgress < duration) {
                    _uiState.value = _uiState.value.copy(progressMs = newProgress)
                } else if (duration > 0 && newProgress >= duration) {
                    // Track finished, refresh state
                    refreshPlaybackState()
                }
            }
        }
    }
    
    /**
     * Toggle play/pause
     */
    fun togglePlayPause() {
        val accessToken = authRepository.getSpotifyAccessToken() ?: return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            val result = if (_uiState.value.isPlaying) {
                SpotifyPlaybackManager.pause(accessToken)
            } else {
                SpotifyPlaybackManager.resume(accessToken)
            }
            
            result.onSuccess {
                // Immediately update UI for responsiveness
                _uiState.value = _uiState.value.copy(
                    isPlaying = !_uiState.value.isPlaying,
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
                
                // Refresh to confirm
                delay(500)
                refreshPlaybackState()
            }.onFailure { exception ->
                Log.e(TAG, "Failed to toggle play/pause", exception)
                _uiState.value = _uiState.value.copy(
                    error = exception.message,
                    isLoading = false
                )
            }
        }
    }
    
    /**
     * Skip to next track
     */
    fun skipToNext() {
        val accessToken = authRepository.getSpotifyAccessToken() ?: return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            val result = SpotifyPlaybackManager.skipToNext(accessToken)
            
            result.onSuccess {
                // Wait a moment for Spotify to switch tracks
                delay(500)
                refreshPlaybackState()
            }.onFailure { exception ->
                Log.e(TAG, "Failed to skip to next", exception)
                _uiState.value = _uiState.value.copy(
                    error = exception.message,
                    isLoading = false
                )
            }
        }
    }
    
    /**
     * Skip to previous track
     */
    fun skipToPrevious() {
        val accessToken = authRepository.getSpotifyAccessToken() ?: return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            val result = SpotifyPlaybackManager.skipToPrevious(accessToken)
            
            result.onSuccess {
                delay(500)
                refreshPlaybackState()
            }.onFailure { exception ->
                Log.e(TAG, "Failed to skip to previous", exception)
                _uiState.value = _uiState.value.copy(
                    error = exception.message,
                    isLoading = false
                )
            }
        }
    }
    
    /**
     * Seek to position in track
     * @param positionMs Position in milliseconds
     */
    fun seekTo(positionMs: Long) {
        val accessToken = authRepository.getSpotifyAccessToken() ?: return
        
        viewModelScope.launch {
            val result = SpotifyPlaybackManager.seekToPosition(accessToken, positionMs)
            
            result.onSuccess {
                // Immediately update UI
                lastKnownProgressMs = positionMs
                lastUpdateTimestamp = System.currentTimeMillis()
                _uiState.value = _uiState.value.copy(progressMs = positionMs)
                
                // Confirm with API
                delay(500)
                refreshPlaybackState()
            }.onFailure { exception ->
                Log.e(TAG, "Failed to seek", exception)
                _uiState.value = _uiState.value.copy(error = exception.message)
            }
        }
    }
    
    /**
     * Manually refresh playback state (pull to refresh, etc.)
     */
    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            refreshPlaybackState()
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        stopPolling()
    }
}
