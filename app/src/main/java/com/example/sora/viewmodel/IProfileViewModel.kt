package com.example.sora.viewmodel

import android.content.Context
import android.net.Uri
import com.example.sora.ui.Song
import kotlinx.coroutines.flow.StateFlow

interface IProfileViewModel{
    // The UI state that contains all auth-related information.
    val uiState: StateFlow<ProfileUiState>
    fun loadProfile(userId: String?)
    fun updateAvatar(context: Context, uri: Uri)
    fun toggleLike(song: Song)
}