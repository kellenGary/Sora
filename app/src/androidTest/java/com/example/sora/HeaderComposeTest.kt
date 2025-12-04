package com.example.sora

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.sora.main.Header
import com.example.sora.ui.theme.SoraTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Compose UI Test 1: Header Component Test
 * 
 * This test verifies that the Header component properly displays
 * the Sora logo and title text.
 */
@RunWith(AndroidJUnit4::class)
class HeaderComposeTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun headerDisplaysSoraLogoAndTitle() {
        // Set up the Compose content
        composeTestRule.setContent {
            SoraTheme {
                Header()
            }
        }
        
        // Verify the Sora logo is displayed
        composeTestRule
            .onNodeWithContentDescription("Sora logo")
            .assertIsDisplayed()
        
        // Verify the "SORA" text is displayed
        composeTestRule
            .onNodeWithText("SORA")
            .assertIsDisplayed()
    }
    
    @Test
    fun headerComponentRendersWithoutCrashing() {
        // Verify the component can be rendered without crashing
        composeTestRule.setContent {
            SoraTheme {
                Header()
            }
        }
        
        // If we get here without exception, the test passes
        composeTestRule.waitForIdle()
    }
}
