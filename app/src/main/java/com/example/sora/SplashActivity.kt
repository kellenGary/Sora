package com.example.sora

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.sora.ui.theme.SoraTheme
import kotlinx.coroutines.delay

class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            SoraTheme {
                // Same gradient as login screen
                val gradientColors = listOf(
                    androidx.compose.ui.graphics.Color(0xFF020618), // Primary
                    androidx.compose.ui.graphics.Color(0xFF0F172B), // Secondary
                    androidx.compose.ui.graphics.Color(0xFF020618)  // Primary
                )
                
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                colors = gradientColors,
                                start = Offset(0f, 0f),
                                end = Offset.Infinite
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.sora),
                        contentDescription = "Sora logo",
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .padding(32.dp)
                    )
                }
                
                LaunchedEffect(Unit) {
                    delay(1500) // Show splash for 1.5 seconds
                    startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                    finish()
                }
            }
        }
    }
}
