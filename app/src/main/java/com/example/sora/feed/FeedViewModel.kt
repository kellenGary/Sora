package com.example.sora.feed

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sora.auth.SupabaseClient
import com.example.sora.data.model.FeedActivity
import com.example.sora.data.model.RawFeedActivity
import com.example.sora.data.model.SharedPlaylist
import com.example.sora.data.repository.UserRepository
import com.example.sora.utils.getLastListen
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class FeedUiState(
    val posts: List<FeedActivity> = emptyList(),
    val playlists: List<SharedPlaylist> = emptyList(),
    val activeFriendsListeners: List<FeedActivity> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isRefreshing: Boolean = false
)

class FeedViewModel : ViewModel() {
    private val feedRepository = FeedRepository()
    private val userRepository = UserRepository()
    private val client = SupabaseClient.supabase
    
    private val _uiState = MutableStateFlow(FeedUiState())
    val uiState: StateFlow<FeedUiState> = _uiState.asStateFlow()

    init {
        loadFeed()
        loadActiveFriends()
    }

    fun loadActiveFriends() {
        viewModelScope.launch {
            try {
                val activeFriends = userRepository.getActiveFriends()
                val activeListeners = activeFriends.mapNotNull { user ->
                    val lastListen = getLastListen(client, user.id)
                    if (lastListen != null && lastListen.id != null) {
                        val raw = client.postgrest["feed_activity"]
                            .select {
                                filter { eq("id", lastListen.id) }
                            }
                            .decodeSingleOrNull<RawFeedActivity>()

                        if (raw != null) {
                            FeedActivity(
                                id = raw.id,
                                userId = raw.userId,
                                userName = raw.userName,
                                userAvatar = raw.userAvatar,
                                songTitle = raw.songTitle,
                                artist = raw.artist,
                                albumCover = raw.albumCover,
                                timestamp = kotlinx.datetime.Instant.parse(raw.timestamp).toEpochMilliseconds(),
                                latitude = raw.latitude,
                                longitude = raw.longitude,
                                activityType = raw.activityType
                            )
                        } else null
                    } else null
                }
                _uiState.value = _uiState.value.copy(activeFriendsListeners = activeListeners)
            } catch (e: Exception) {
                Log.e("FeedViewModel", "Error loading active friends", e)
            }
        }
    }

    fun loadFeed() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            val result = feedRepository.getFriendsFeed()
            val playlists = feedRepository.getSharedPlaylists()
            
            result.onSuccess { posts ->
                _uiState.value = _uiState.value.copy(
                    posts = posts,
                    isLoading = false,
                    errorMessage = null
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = error.message ?: "Failed to load feed"
                )
            }
            playlists.onSuccess { playlists ->
                _uiState.value = _uiState.value.copy(
                    playlists = playlists,
                    isLoading = false,
                    errorMessage = null
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = error.message ?: "Failed to load playlists"
                )
            }
        }
    }

    fun refreshFeed() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true)
            
            val result = feedRepository.getFriendsFeed()
            loadActiveFriends()
            
            result.onSuccess { posts ->
                _uiState.value = _uiState.value.copy(
                    posts = posts,
                    isRefreshing = false,
                    errorMessage = null
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isRefreshing = false,
                    errorMessage = error.message ?: "Failed to refresh feed"
                )
            }
        }
    }
}
