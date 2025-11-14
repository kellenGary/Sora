package com.example.sora.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sora.auth.AuthRepository
import com.example.sora.ui.Song
import com.example.sora.user.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

data class ProfileUiState(
    val displayName: String? = null,
    val avatarUrl: String? = null,
    val uniqueSongs: Int = 0,
    val listeningHistory: List<Song> = emptyList(),
    val likedSongs: List<Song> = emptyList()
)

private const val TAG = "ProfileViewModel"

class ProfileViewModel: ViewModel(), IProfileViewModel {
    private val userRepository = UserRepository();
    private val authRepository = AuthRepository();

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
            val idToLoad = userId ?: authRepository.getCurrentUser()?.id
            if (idToLoad != null) {
                val profile = userRepository.getUser(idToLoad)
                _uiState.value = _uiState.value.copy(
                    displayName = profile?.displayName ?: "User",
                    avatarUrl = profile?.avatarUrl
                    // TODO: populate uniqueSongs, listeningHistory, likedSongs if available
                )
                Log.d(TAG, "Loaded profile for $idToLoad: ${profile?.displayName}")
            } else {
                Log.e(TAG, "No userId available to load profile")
            }
        }
    }

}