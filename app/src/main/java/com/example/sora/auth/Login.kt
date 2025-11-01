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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(8, 44, 71))
    ) {
        Image(
            painter = painterResource(id = R.drawable.sora),
            contentDescription = "Sora logo",
            modifier = Modifier
                .padding(top = 100.dp).fillMaxWidth()
        )
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
            colors = ButtonDefaults.buttonColors(containerColor = Color(29, 185, 84)),
            modifier = Modifier.width(200.dp).align(Alignment.Center),
            enabled = !uiState.isLoading
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Text("Login with Spotify", color = Color.White)
            }
        }
        
        // Show error message if any
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
