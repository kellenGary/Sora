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

object SongLocationGenerator {
    /**
     * Generates dummy song locations around a center point
     * @param centerLocation The user's current location
     * @param count Number of dummy locations to generate
     * @param maxDistanceMeters Maximum distance from center (default 5km)
     */
    fun generateDummyLocations(
        centerLocation: LatLng,
        count: Int = 15,
        maxDistanceMeters: Double = 5000.0
    ): List<SongLocation> {
        println("SongLocationGenerator: Generating $count locations around $centerLocation")
        val dummySongs = listOf(
            "Blinding Lights" to "The Weeknd",
            "Shape of You" to "Ed Sheeran",
            "Someone Like You" to "Adele",
            "Levitating" to "Dua Lipa",
            "Starboy" to "The Weeknd",
            "Perfect" to "Ed Sheeran",
            "Rolling in the Deep" to "Adele",
            "Don't Start Now" to "Dua Lipa",
            "Save Your Tears" to "The Weeknd",
            "Bad Habits" to "Ed Sheeran",
            "Hello" to "Adele",
            "Physical" to "Dua Lipa",
            "Can't Feel My Face" to "The Weeknd",
            "Thinking Out Loud" to "Ed Sheeran",
            "Easy On Me" to "Adele",
            "Break My Heart" to "Dua Lipa",
            "Die For You" to "The Weeknd",
            "Photograph" to "Ed Sheeran",
            "Set Fire to the Rain" to "Adele",
            "New Rules" to "Dua Lipa"
        )

        val locations = List(count) { index ->
            val (song, artist) = dummySongs[index % dummySongs.size]
            
            // Generate random offset from center location
            val randomLocation = generateRandomLocationNearby(
                centerLocation,
                maxDistanceMeters
            )
            
            // Random radius between 50m and 500m
            val radius = (50..500).random().toDouble()
            
            // Random timestamp within the last 30 days
            val timestamp = System.currentTimeMillis() - (0..30).random() * 24 * 60 * 60 * 1000L
            
            SongLocation(
                id = "song_$index",
                songTitle = song,
                artist = artist,
                location = randomLocation,
                radiusMeters = radius,
                timestamp = timestamp
            )
        }
        println("SongLocationGenerator: Created ${locations.size} locations")
        locations.forEach { println("  - ${it.songTitle} at ${it.location}") }
        return locations
    }

    /**
     * Generates a random location within a certain distance from a center point
     */
    private fun generateRandomLocationNearby(
        center: LatLng,
        maxDistanceMeters: Double
    ): LatLng {
        // Earth's radius in meters
        val earthRadius = 6371000.0
        
        // Random distance and bearing
        val distance = Math.random() * maxDistanceMeters
        val bearing = Math.random() * 2 * Math.PI
        
        // Convert distance to radians
        val distanceRad = distance / earthRadius
        val centerLatRad = Math.toRadians(center.latitude)
        val centerLngRad = Math.toRadians(center.longitude)
        
        // Calculate new latitude
        val newLatRad = Math.asin(
            Math.sin(centerLatRad) * Math.cos(distanceRad) +
            Math.cos(centerLatRad) * Math.sin(distanceRad) * Math.cos(bearing)
        )
        
        // Calculate new longitude
        val newLngRad = centerLngRad + Math.atan2(
            Math.sin(bearing) * Math.sin(distanceRad) * Math.cos(centerLatRad),
            Math.cos(distanceRad) - Math.sin(centerLatRad) * Math.sin(newLatRad)
        )
        
        return LatLng(
            Math.toDegrees(newLatRad),
            Math.toDegrees(newLngRad)
        )
    }
}
