package com.example.sora.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sora.auth.AuthRepository
import com.example.sora.data.repository.LikedSongsRepository
import com.example.sora.ui.Song
import com.example.sora.data.repository.UserRepository
import com.example.sora.data.repository.UserStatsRepository
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
    val listeningHistory: List<Song> = emptyList(),
    val likedSongs: List<Song> = emptyList()
)

private const val TAG = "ProfileViewModel"

class ProfileViewModel: ViewModel(), IProfileViewModel {
    private val userRepository = UserRepository()
    private val authRepository = AuthRepository()
    private val userStatsRepository = UserStatsRepository()
    private val likedSongsRepository = LikedSongsRepository();

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

            if (idToLoad == null) {
                Log.e(TAG, "No userId available to load profile")
                return@launch
            }

            val profile = userRepository.getUser(idToLoad)

            val likedSongIds = likedSongsRepository.getLikedSongIds()
            val likedSongsUi = likedSongIds.map { songId ->
                // For simplicity, map liked song IDs to Song objects
                // You might want to fetch full song info if available
                Song(
                    id = songId,
                    title = "Unknown",
                    artist = "Unknown",
                    albumArtUrl = null,
                    isLiked = true
                )
            }

            val fullHistory = userStatsRepository.getFullListeningHistory(
                userId = idToLoad,
                limit = 10
            )

            val historyUiSongs = fullHistory.map { row ->
                Song(
                    id = row.song_id,
                    title = row.song_title,
                    artist = row.artist_name,
                    albumArtUrl = row.album_cover,
                    isLiked = likedSongIds.contains(row.song_id)
                )
            }

            val uniqueSongs = userStatsRepository.getUniqueSongCount(idToLoad)

            _uiState.value = _uiState.value.copy(
                displayName = profile?.displayName ?: "User",
                avatarUrl = profile?.avatarUrl,
                listeningHistory = historyUiSongs,
                likedSongs = likedSongsUi,
                uniqueSongs = uniqueSongs,
                isPersonalProfile = (currentUserId == idToLoad),
                // TODO: populate uniqueSongs, listeningHistory, likedSongs if available
            )
            Log.d(TAG, "Loaded profile for $idToLoad: ${profile?.displayName}")
        }
    }

    override fun toggleLike(song: Song) {
        viewModelScope.launch {
            val currentUser = authRepository.getCurrentUser() ?: return@launch
            val updatedHistory = _uiState.value.listeningHistory.map {
                if (it.id == song.id) {
                    if (it.isLiked) likedSongsRepository.unlikeSong(it.id)
                    else likedSongsRepository.likeSong(it.id)
                    it.copy(isLiked = !it.isLiked)
                } else it
            }

            val updatedLikes = _uiState.value.likedSongs.toMutableList()
            if (song.isLiked) {
                updatedLikes.removeAll { it.id == song.id }
            } else {
                updatedLikes.add(song.copy(isLiked = true))
            }

            _uiState.value = _uiState.value.copy(
                listeningHistory = updatedHistory,
                likedSongs = updatedLikes
            )
        }
    }

}