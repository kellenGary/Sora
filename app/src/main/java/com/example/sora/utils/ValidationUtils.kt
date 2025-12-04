package com.example.sora.utils

/**
 * Utility class for common validation operations
 */
object ValidationUtils {
    
    /**
     * Validates if a display name is acceptable
     * @param displayName the name to validate
     * @return true if valid, false otherwise
     */
    fun isValidDisplayName(displayName: String?): Boolean {
        if (displayName.isNullOrBlank()) return false
        if (displayName.length < 2) return false
        if (displayName.length > 50) return false
        return displayName.matches(Regex("^[a-zA-Z0-9_\\s]+$"))
    }
    
    /**
     * Validates geographic coordinates
     * @param latitude the latitude value
     * @param longitude the longitude value
     * @return true if coordinates are valid, false otherwise
     */
    fun isValidCoordinates(latitude: Double, longitude: Double): Boolean {
        return latitude in -90.0..90.0 && longitude in -180.0..180.0
    }
    
    /**
     * Formats a song title with artist name
     * @param songTitle the title of the song
     * @param artistName the name of the artist
     * @return formatted string "Song Title - Artist Name"
     */
    fun formatSongWithArtist(songTitle: String, artistName: String): String {
        return "$songTitle - $artistName"
    }
    
    /**
     * Calculates distance between two geographic points using Haversine formula
     * @param lat1 latitude of first point
     * @param lon1 longitude of first point
     * @param lat2 latitude of second point
     * @param lon2 longitude of second point
     * @return distance in kilometers
     */
    fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadius = 6371.0 // kilometers
        
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        
        return earthRadius * c
    }
}
