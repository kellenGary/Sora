package com.example.sora.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sora.auth.AuthRepository
import com.example.sora.data.repository.FollowRepository
import com.example.sora.data.repository.LikedSongsRepository
import com.example.sora.data.repository.UserRepository
import com.example.sora.data.repository.UserStatsRepository
import com.example.sora.ui.SongUi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

data class ProfileUiState(
    val displayName: String? = null,
    val avatarUrl: String? = null,
    val isPersonalProfile: Boolean = false,
    val uniqueSongs: Int = 0,
    val listeningHistory: List<SongUi> = emptyList(),
    val likedSongs: List<SongUi> = emptyList(),
    val isFollowed: Boolean = false
)

private const val TAG = "ProfileViewModel"

class ProfileViewModel: ViewModel(), IProfileViewModel {
    private val userRepository = UserRepository()
    private val authRepository = AuthRepository()
    private val userStatsRepository = UserStatsRepository()
    private val likedSongsRepository = LikedSongsRepository()
    private val followRepository = FollowRepository()

    private val _uiState = MutableStateFlow(ProfileUiState())
    override val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()




    override fun updateAvatar(context: Context, uri: Uri) {
        authRepository.getCurrentUser()?.id?.let { userId ->
            viewModelScope.launch {
                try {
                    val inputStream = context.contentResolver.openInputStream(uri)

                    val tempFile = File.createTempFile("avatar_", ".jpg", context.cacheDir)
                    tempFile.outputStream().use { output ->
                        inputStream?.copyTo(output)
                    }

                    val result = userRepository.uploadProfilePicture(tempFile)

                    result.onSuccess { newAvatarUrl ->
                        // Coil's Async Image aggressivley caches so we need to add a cache buster
                        val urlWithCacheBuster = "$newAvatarUrl?t=${System.currentTimeMillis()}"
                        _uiState.value = _uiState.value.copy(avatarUrl = urlWithCacheBuster)
                        Log.d(TAG, "Avatar updated successfully: $newAvatarUrl")
                    }.onFailure { e ->
                        Log.e(TAG, "Upload failed: ${e.message}")
                    }

                } catch (e: Exception) {
                    Log.e(TAG, "Failed to copy file: ${e.message}")
                }
            }
        }
    }

    override fun loadProfile(userId: String?) {
        viewModelScope.launch {
            val currentUserId = authRepository.getCurrentUser()?.id
            val idToLoad = userId ?: authRepository.getCurrentUser()?.id

            if (idToLoad == null || currentUserId == null) {
                Log.e(TAG, "No userId available to load profile or not logged in")
                return@launch
            }

            val profile = userRepository.getUser(idToLoad)

            val likedSongsUi = likedSongsRepository.getLikedSongs(idToLoad)

            val personalLikedSongs = likedSongsRepository.getLikedSongs(currentUserId)
            val likedSongIdsSet = personalLikedSongs.map { it.id }.toSet()

            val fullHistory = userStatsRepository.getFullListeningHistory(
                userId = idToLoad,
                limit = 10
            )

            val historyUiSongs = fullHistory.map { row ->
                SongUi(
                    id = row.song_id,
                    title = row.song_title,
                    artist = row.artist_name,
                    albumArtUrl = row.album_cover,
                    isLiked = likedSongIdsSet.contains(row.song_id)
                )
            }

            val uniqueSongs = userStatsRepository.getUniqueSongCount(idToLoad)

            val isFollowed = if (currentUserId != idToLoad) {
                followRepository.isFollowing(idToLoad)
            } else false

            _uiState.value = _uiState.value.copy(
                displayName = profile?.displayName ?: "User",
                avatarUrl = profile?.avatarUrl,
                listeningHistory = historyUiSongs,
                likedSongs = likedSongsUi,
                uniqueSongs = uniqueSongs,
                isPersonalProfile = (currentUserId == idToLoad),
                isFollowed = isFollowed,
                // TODO: populate uniqueSongs, listeningHistory, likedSongs if available
            )
            Log.d(TAG, "Loaded profile for $idToLoad: ${profile?.displayName}")
        }
    }

    override fun toggleLike(song: SongUi) {
        viewModelScope.launch {
            if (authRepository.getCurrentUser() == null) return@launch

            // Perform DB update first
            val success = if (song.isLiked) {
                likedSongsRepository.unlikeSong(song.id)
            } else {
                likedSongsRepository.likeSong(song.id)
            }


            if (!success) return@launch


            val updatedHistory = _uiState.value.listeningHistory.map {
                if (it.id == song.id) it.copy(isLiked = !it.isLiked)
                else it
            }

            val updatedLikes = if (_uiState.value.isPersonalProfile) {
                val currentLikes = _uiState.value.likedSongs.toMutableList()
                if (song.isLiked) {
                    currentLikes.removeAll { it.id == song.id }
                } else {
                    currentLikes.add(song.copy(isLiked = true))
                }
                currentLikes
            } else {
                _uiState.value.likedSongs // leave as is
            }

            _uiState.value = _uiState.value.copy(
                listeningHistory = updatedHistory,
                likedSongs = updatedLikes
            )

        }
    }

    override fun follow(userId: String) {
        viewModelScope.launch {
            followRepository.followUser(userId)

            _uiState.value = _uiState.value.copy(isFollowed = true)
        }
    }

    override fun unfollow(userId: String) {
        viewModelScope.launch {
            followRepository.unfollowUser(userId)
            _uiState.value = _uiState.value.copy(isFollowed = false)
        }
    }

}