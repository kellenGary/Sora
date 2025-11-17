package com.example.sora.map

import com.google.android.gms.maps.model.LatLng

data class SongLocation(
    val id: String,
    val songTitle: String,
    val artist: String,
    val location: LatLng,
    val radiusMeters: Double,
    val timestamp: Long,
    val albumArtUrl: String? = null
)