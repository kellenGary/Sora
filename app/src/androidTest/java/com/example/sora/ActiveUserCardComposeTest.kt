package com.example.sora

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.sora.main.ActiveUserCard
import com.example.sora.ui.theme.SoraTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Compose UI Test 2: ActiveUserCard Component Test
 * 
 * This test verifies that the ActiveUserCard component displays
 * user information correctly and handles click interactions.
 */
@RunWith(AndroidJUnit4::class)
class ActiveUserCardComposeTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun activeUserCardDisplaysUserName() {
        // Set up test data
        val testUserName = "John Doe"
        val testSongTitle = "Bohemian Rhapsody"
        val testArtist = "Queen"
        
        // Set up the Compose content
        composeTestRule.setContent {
            SoraTheme {
                ActiveUserCard(
                    userName = testUserName,
                    userAvatar = null,
                    songTitle = testSongTitle,
                    artist = testArtist,
                    albumCover = null,
                    onClick = {}
                )
            }
        }
        
        // Verify the username is displayed
        composeTestRule
            .onNodeWithText(testUserName)
            .assertIsDisplayed()
    }
    
    @Test
    fun activeUserCardHandlesClickInteraction() {
        // Set up test data
        var clickCount = 0
        val testUserName = "Jane Smith"
        val testSongTitle = "Imagine"
        val testArtist = "John Lennon"
        
        // Set up the Compose content with click handler
        composeTestRule.setContent {
            SoraTheme {
                ActiveUserCard(
                    userName = testUserName,
                    userAvatar = null,
                    songTitle = testSongTitle,
                    artist = testArtist,
                    albumCover = null,
                    onClick = { clickCount++ }
                )
            }
        }
        
        // Perform click on the username
        composeTestRule
            .onNodeWithText(testUserName)
            .performClick()
        
        // Wait for idle to ensure click is processed
        composeTestRule.waitForIdle()
        
        // Verify click was registered (would need to expose state in real implementation)
        // For now, we verify the component doesn't crash on click
        assert(clickCount == 1) { "Click handler should have been called once" }
    }
    
    @Test
    fun activeUserCardDisplaysInitialWhenNoAvatar() {
        // Set up test data
        val testUserName = "Alice"
        
        // Set up the Compose content
        composeTestRule.setContent {
            SoraTheme {
                ActiveUserCard(
                    userName = testUserName,
                    userAvatar = null,
                    songTitle = "Test Song",
                    artist = "Test Artist",
                    albumCover = null,
                    onClick = {}
                )
            }
        }
        
        // Verify the first letter of username is displayed as initial
        composeTestRule
            .onNodeWithText("A")
            .assertIsDisplayed()
        
        // Verify username is still displayed below avatar
        composeTestRule
            .onNodeWithText(testUserName)
            .assertIsDisplayed()
    }
}
