package com.example.sora.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Artist(
    val id: String? = null,
    val name: String,
    val link: String,
    @SerialName("created_at")
    val createdAt: String? = null
)

@Serializable
data class ArtistInsert(
    val name: String,
    val link: String
)

@Serializable
data class Album(
    val id: String? = null,
    val name: String,
    val cover: String?,
    val link: String,
    @SerialName("release_date")
    val releaseDate: String?,
    @SerialName("artist_id")
    val artistId: String,
    @SerialName("created_at")
    val createdAt: String? = null
)

@Serializable
data class AlbumInsert(
    val name: String,
    val cover: String?,
    val link: String,
    @SerialName("release_date")
    val releaseDate: String?,
    @SerialName("artist_id")
    val artistId: String
)

@Serializable
data class Song(
    val id: String? = null,
    val title: String,
    @SerialName("artist_id")
    val artistId: String,
    @SerialName("album_id")
    val albumId: String,
    @SerialName("created_at")
    val createdAt: String? = null
)

@Serializable
data class SongInsert(
    val title: String,
    @SerialName("artist_id")
    val artistId: String,
    @SerialName("album_id")
    val albumId: String
)

@Serializable
data class ListenHistory(
    val id: String? = null,
    @SerialName("user_id")
    val userId: String,
    @SerialName("song_id")
    val songId: String,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long,
    @SerialName("created_at")
    val createdAt: String? = null
)
