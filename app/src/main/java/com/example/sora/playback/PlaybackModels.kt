package com.example.sora.playback

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SpotifyImage(
    val url: String,
    val height: Int?,
    val width: Int?
)

@Serializable
data class SpotifyArtist(
    val id: String,
    val name: String,
    val uri: String
)

@Serializable
data class SpotifyAlbum(
    val id: String,
    val name: String,
    val images: List<SpotifyImage>,
    @SerialName("release_date")
    val releaseDate: String?,
    val uri: String
)

@Serializable
data class SpotifyTrack(
    val id: String,
    val name: String,
    val artists: List<SpotifyArtist>,
    val album: SpotifyAlbum,
    @SerialName("duration_ms")
    val durationMs: Long,
    val uri: String,
    val explicit: Boolean = false
)

@Serializable
data class SpotifyDevice(
    val id: String?,
    val name: String,
    val type: String,
    @SerialName("is_active")
    val isActive: Boolean,
    @SerialName("is_private_session")
    val isPrivateSession: Boolean = false,
    @SerialName("is_restricted")
    val isRestricted: Boolean = false,
    @SerialName("volume_percent")
    val volumePercent: Int?,
    @SerialName("supports_volume")
    val supportsVolume: Boolean = true
)

@Serializable
data class SpotifyPlaybackState(
    val timestamp: Long,
    @SerialName("progress_ms")
    val progressMs: Long?,
    @SerialName("is_playing")
    val isPlaying: Boolean,
    val item: SpotifyTrack?,
    @SerialName("currently_playing_type")
    val currentlyPlayingType: String,
    val device: SpotifyDevice?,
    @SerialName("shuffle_state")
    val shuffleState: Boolean = false,
    @SerialName("smart_shuffle")
    val smartShuffle: Boolean = false,
    @SerialName("repeat_state")
    val repeatState: String = "off"
)

// Simplified state for UI
data class PlaybackUiState(
    val track: SpotifyTrack? = null,
    val isPlaying: Boolean = false,
    val progressMs: Long = 0L,
    val durationMs: Long = 0L,
    val isLoading: Boolean = false,
    val error: String? = null,
    val hasActiveDevice: Boolean = false
)
