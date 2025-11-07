package com.example.sora.auth

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sora.R
import com.example.sora.features.SpotifyAuthManager
import androidx.navigation.NavController

@Composable
fun Login(navController: NavController, authViewModel: AuthViewModel = viewModel()) {
    val context = LocalContext.current
    
    // Clear old messages immediately when this screen appears
    LaunchedEffect(Unit) {
        authViewModel.clearMessages()
    }

    DisposableEffect(Unit) {
        Log.d("Login", "onCreateView called")
        onDispose {
            Log.d("Login", "onDestroyView called")
        }
    }

    val uiState by authViewModel.uiState.collectAsState()

    // Navigate to main screen if login successful
    LaunchedEffect(uiState.isLoggedIn) {
        if (uiState.isLoggedIn) {
            navController.navigate("main") {
                popUpTo("login") { inclusive = true }
            }
        }
        authViewModel.clearMessages()
    }

    val gradientColors = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.primary
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = gradientColors,
                    start = Offset(0f, 0f), // Top left
                    end = Offset.Infinite // Bottom right
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Sora Logo
            Image(
                painter = painterResource(id = R.drawable.sora),
                contentDescription = "Sora logo",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 48.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) { 
                Text(
                    text = "Welcome to Sora",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    modifier = Modifier.padding(top = 24.dp)
                )
                Text(
                    text = "Connect with friends and discover what music is playing around you",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF94A3B8), // slate-400
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            
            // Feature Cards
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Friends' Listening History Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.1f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Icon container
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    color = Color(0x1A1DB954), // Green with 10% opacity
                                    shape = MaterialTheme.shapes.medium
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_friends),
                                contentDescription = null,
                                tint = Color(0xFF1DB954), // Spotify green
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        
                        // Text content
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Friends' Listening History",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "See what your friends are jamming to in real-time",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
                
                // Nearby Music Discovery Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.1f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Icon container
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    color = Color(0x1A3B82F6), // Blue with 10% opacity
                                    shape = MaterialTheme.shapes.medium
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_map),
                                contentDescription = null,
                                tint = Color(0xFF3B82F6), // Blue
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        
                        // Text content
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Nearby Music Discovery",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Explore what people near you are listening to right now",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
                
                // Daily Recommendations Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.1f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Icon container
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    color = Color(0x1AA855F7), // Purple with 10% opacity
                                    shape = MaterialTheme.shapes.medium
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_music),
                                contentDescription = null,
                                tint = Color(0xFFA855F7), // Purple
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        
                        // Text content
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Daily Recommendations",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Find new music based on your community's taste",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }
            
            // Login Button
        Button(
            onClick = {
                Log.d("SpotifyAuth", "Login with Spotify button clicked")
                authViewModel.setSpotifyLoading(true)
                try {
                    val intent = SpotifyAuthManager.getAuthorizationRequestIntent(context)
                    context.startActivity(intent)
                } catch (e: Exception) {
                    Log.e("SpotifyAuth", "Failed to start Spotify auth: ${e.message}")
                    authViewModel.setErrorMessage("Failed to start Spotify authentication")
                    authViewModel.setSpotifyLoading(false)
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1DB954)),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = !uiState.isLoading
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Icon(
                    painter = painterResource(id = R.drawable.ic_spotify),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Login with Spotify", color = Color.White)
            }
        }
        }
        
        // Show error message if any - positioned at bottom of Box
        uiState.errorMessage?.let { error ->
            Snackbar(
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.BottomCenter)
            ) {
                Text(error)
            }
        }
    }
}
