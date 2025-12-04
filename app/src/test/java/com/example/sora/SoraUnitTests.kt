package com.example.sora

import com.example.sora.data.model.User
import com.example.sora.data.model.Song
import com.example.sora.data.model.Artist
import com.example.sora.data.model.ListenHistory
import com.example.sora.utils.ValidationUtils
import org.junit.Test
import org.junit.Assert.*
import org.junit.Before

/**
 * Unit tests for Sora app
 * 
 * These tests verify:
 * 1. Data model creation and serialization
 * 2. Validation utilities
 * 3. Business logic functions
 */
class SoraUnitTests {
    
    private lateinit var testUser: User
    private lateinit var testArtist: Artist
    private lateinit var testSong: Song
    
    @Before
    fun setup() {
        // Initialize test data before each test
        testUser = User(
            id = "user123",
            displayName = "Test User",
            avatarUrl = "https://example.com/avatar.jpg",
            createdAt = "2024-01-01T00:00:00Z",
            isActive = true
        )
        
        testArtist = Artist(
            id = "artist123",
            name = "Test Artist",
            link = "https://spotify.com/artist/123",
            createdAt = "2024-01-01T00:00:00Z"
        )
        
        testSong = Song(
            id = "song123",
            title = "Test Song",
            artistId = "artist123",
            albumId = "album123",
            createdAt = "2024-01-01T00:00:00Z"
        )
    }
    
    /**
     * Test 1: User Model Validation
     * Tests that User data model properly stores and retrieves values
     */
    @Test
    fun testUserModelCreation() {
        // Assert user properties are correctly set
        assertEquals("user123", testUser.id)
        assertEquals("Test User", testUser.displayName)
        assertEquals("https://example.com/avatar.jpg", testUser.avatarUrl)
        assertTrue(testUser.isActive)
        assertNotNull(testUser.createdAt)
        
        // Test user with null optional fields
        val minimalUser = User(id = "user456")
        assertEquals("user456", minimalUser.id)
        assertNull(minimalUser.displayName)
        assertNull(minimalUser.avatarUrl)
        assertFalse(minimalUser.isActive)
    }
    
    /**
     * Test 2: Display Name Validation
     * Tests ValidationUtils.isValidDisplayName with various inputs
     */
    @Test
    fun testDisplayNameValidation() {
        // Valid display names
        assertTrue(ValidationUtils.isValidDisplayName("John Doe"))
        assertTrue(ValidationUtils.isValidDisplayName("User123"))
        assertTrue(ValidationUtils.isValidDisplayName("test_user"))
        assertTrue(ValidationUtils.isValidDisplayName("AB"))
        
        // Invalid display names
        assertFalse(ValidationUtils.isValidDisplayName(null))
        assertFalse(ValidationUtils.isValidDisplayName(""))
        assertFalse(ValidationUtils.isValidDisplayName(" "))
        assertFalse(ValidationUtils.isValidDisplayName("A")) // too short
        assertFalse(ValidationUtils.isValidDisplayName("A".repeat(51))) // too long
        assertFalse(ValidationUtils.isValidDisplayName("user@123")) // special characters
        assertFalse(ValidationUtils.isValidDisplayName("user#name")) // special characters
        assertFalse(ValidationUtils.isValidDisplayName("user.name")) // dots not allowed
    }
    
    /**
     * Test 3: Geographic Coordinates Validation and Distance Calculation
     * Tests coordinate validation and distance calculation between two points
     */
    @Test
    fun testGeographicFunctions() {
        // Test valid coordinates
        assertTrue(ValidationUtils.isValidCoordinates(0.0, 0.0)) // Equator, Prime Meridian
        assertTrue(ValidationUtils.isValidCoordinates(40.7128, -74.0060)) // New York
        assertTrue(ValidationUtils.isValidCoordinates(-33.8688, 151.2093)) // Sydney
        assertTrue(ValidationUtils.isValidCoordinates(90.0, 180.0)) // Extreme valid values
        assertTrue(ValidationUtils.isValidCoordinates(-90.0, -180.0)) // Extreme valid values
        
        // Test invalid coordinates
        assertFalse(ValidationUtils.isValidCoordinates(91.0, 0.0)) // Latitude too high
        assertFalse(ValidationUtils.isValidCoordinates(-91.0, 0.0)) // Latitude too low
        assertFalse(ValidationUtils.isValidCoordinates(0.0, 181.0)) // Longitude too high
        assertFalse(ValidationUtils.isValidCoordinates(0.0, -181.0)) // Longitude too low
        
        // Test distance calculation
        // Distance from New York to Los Angeles (approx 3944 km)
        val nyToLa = ValidationUtils.calculateDistance(
            40.7128, -74.0060,  // New York
            34.0522, -118.2437  // Los Angeles
        )
        assertTrue(nyToLa > 3900.0 && nyToLa < 4000.0)
        
        // Distance from same point should be 0
        val samePoint = ValidationUtils.calculateDistance(
            40.7128, -74.0060,
            40.7128, -74.0060
        )
        assertEquals(0.0, samePoint, 0.001)
        
        // Test ListenHistory with valid coordinates
        val listenHistory = ListenHistory(
            id = "history123",
            userId = "user123",
            songId = "song123",
            latitude = 40.7128,
            longitude = -74.0060,
            timestamp = System.currentTimeMillis()
        )
        assertTrue(ValidationUtils.isValidCoordinates(
            listenHistory.latitude,
            listenHistory.longitude
        ))
    }
    
    /**
     * Test 4: Song Formatting
     * Tests the formatSongWithArtist utility function
     */
    @Test
    fun testSongFormatting() {
        val formatted = ValidationUtils.formatSongWithArtist("Test Song", "Test Artist")
        assertEquals("Test Song - Test Artist", formatted)
        
        val formatted2 = ValidationUtils.formatSongWithArtist("Bohemian Rhapsody", "Queen")
        assertEquals("Bohemian Rhapsody - Queen", formatted2)
        
        // Test with empty strings
        val emptyFormat = ValidationUtils.formatSongWithArtist("", "")
        assertEquals(" - ", emptyFormat)
    }
    
    /**
     * Test 5: Song Model Properties
     * Tests Song data model creation and field access
     */
    @Test
    fun testSongModelProperties() {
        assertEquals("song123", testSong.id)
        assertEquals("Test Song", testSong.title)
        assertEquals("artist123", testSong.artistId)
        assertEquals("album123", testSong.albumId)
        assertNotNull(testSong.createdAt)
        
        // Verify formatted output
        val formattedSong = ValidationUtils.formatSongWithArtist(
            testSong.title,
            testArtist.name
        )
        assertEquals("Test Song - Test Artist", formattedSong)
    }
}
