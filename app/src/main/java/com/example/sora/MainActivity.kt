package com.example.sora

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.sora.library.main.LibraryScreen
import com.example.sora.auth.AuthViewModel
import com.example.sora.auth.Login
import com.example.sora.auth.SpotifyTokenManager
import com.example.sora.auth.SpotifyTokenRefresher
import com.example.sora.features.SpotifyAuthManager
import com.example.sora.friends.FriendScreen
import com.example.sora.library.playlists.PlaylistScreen
import com.example.sora.map.MapScreen
import com.example.sora.playback.PlaybackViewModel
import com.example.sora.playback.ui.ExpandedPlayer
import com.example.sora.playback.ui.MiniPlayer
import com.example.sora.service.SongTrackingService
import com.example.sora.ui.BottomNavBar
import com.example.sora.ui.MainScreen
import com.example.sora.ui.ProfileScreen
import com.example.sora.ui.settings.optionScreens.ChangePasswordScreen
import com.example.sora.ui.settings.SettingScreen
import com.example.sora.ui.settings.optionScreens.LinkedAccountScreen
import com.example.sora.utils.PermissionHandler
import com.example.sora.viewmodel.ProfileViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val TAG = "MainActivity"

class MainActivity : ComponentActivity() {
    private lateinit var authViewModel: AuthViewModel
    private lateinit var profileViewModel: ProfileViewModel
    private lateinit var playbackViewModel: PlaybackViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate(Bundle?) called")
        Log.d(TAG, "Intent: ${intent?.data}")
        Log.d(TAG, "Intent action: ${intent?.action}")

        enableEdgeToEdge()
        setContent {
            com.example.sora.ui.theme.SoraTheme {
                val navController = rememberNavController()
                authViewModel = viewModel()
                profileViewModel = viewModel()
                playbackViewModel = viewModel()

            // Handle Spotify callback intent
            LaunchedEffect(intent) {
                handleSpotifyCallback(intent, authViewModel)
            }

            // Auto-login and token refresh on app startup
            val tokenManager = SpotifyTokenManager.getInstance(this@MainActivity)
            LaunchedEffect(Unit) {
                // If user has stored credentials but isn't logged in, do full auto-login
                if (tokenManager.hasStoredCredentials() && !authViewModel.uiState.value.isLoggedIn) {
                    Log.d(TAG, "Found stored credentials, attempting auto-login to Supabase")
                    
                    val email = tokenManager.getUserEmail()
                    val password = tokenManager.getSupabasePassword()
                    
                    if (email != null && password != null) {
                        // First, sign into Supabase to establish session
                        CoroutineScope(Dispatchers.Main).launch {
                            val authRepository = com.example.sora.auth.AuthRepository(this@MainActivity)
                            authRepository.signIn(email, password).onSuccess {
                                Log.d(TAG, "Successfully logged into Supabase, now refreshing Spotify token")
                                
                                // Now refresh Spotify token
                                SpotifyTokenRefresher.refreshAccessToken(this@MainActivity).onSuccess { tokenResponse ->
                                    Log.d(TAG, "Spotify token refreshed successfully")
                                    
                                    // Update AuthViewModel state with refreshed tokens
                                    authViewModel.handleLocalTokenRefresh(
                                        tokenResponse.accessToken,
                                        tokenResponse.refreshToken ?: tokenManager.getRefreshToken()!!,
                                        tokenResponse.expiresIn
                                    )
                                }.onFailure {
                                    Log.e(TAG, "Failed to refresh Spotify token on startup", it)
                                }
                            }.onFailure {
                                Log.e(TAG, "Failed to sign into Supabase on startup", it)
                            }
                        }
                    } else {
                        Log.w(TAG, "Missing email or password for auto-login")
                    }
                } else if (tokenManager.hasStoredCredentials() && authViewModel.uiState.value.isLoggedIn) {
                    // User is logged in, just refresh the Spotify token in background
                    Log.d(TAG, "User already logged in, refreshing Spotify token in background")
                    SpotifyTokenRefresher.refreshAccessToken(this@MainActivity).onSuccess {
                        // Update AuthViewModel state
                        authViewModel.refreshSpotifyStatus(null)
                    }
                }
            }

            // Start song tracking service when user is logged in and Spotify is connected
            LaunchedEffect(authViewModel.uiState.value.isLoggedIn, authViewModel.uiState.value.isSpotifyConnected) {
                if (authViewModel.uiState.value.isLoggedIn && authViewModel.uiState.value.isSpotifyConnected) {
                    Log.d(TAG, "User logged in and Spotify connected - checking permissions")
                    
                    // Request permissions if needed
                    if (!PermissionHandler.hasLocationPermission(this@MainActivity) ||
                        !PermissionHandler.hasNotificationPermission(this@MainActivity)) {
                        Log.d(TAG, "Requesting permissions")
                        PermissionHandler.requestAllPermissions(this@MainActivity)
                    }
                    
                    // Start service
                    Log.d(TAG, "Starting song tracking service")
                    SongTrackingService.start(this@MainActivity)
                } else {
                    // Stop service if user logs out or disconnects Spotify
                    Log.d(TAG, "Stopping song tracking service")
                    SongTrackingService.stop(this@MainActivity)
                }
            }

            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route

            // Determine start destination based on stored credentials
            val startDestination = if (tokenManager.hasStoredCredentials()) "main" else "login"
            
            // Navigate to main screen once login completes
            LaunchedEffect(authViewModel.uiState.value.isLoggedIn) {
                if (authViewModel.uiState.value.isLoggedIn && currentRoute == "login") {
                    navController.navigate("main") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            }
            
            Box(modifier = Modifier.fillMaxSize()) {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                NavHost(
                    navController = navController,
                    startDestination = startDestination,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    composable("login") {
                        Login(navController, authViewModel)
                    }
                    composable("main") {
                        MainScreen(navController, authViewModel)
                    }
                    composable("map") {
                         MapScreen(navController)
                    }
                    composable("library") {
                        LibraryScreen(navController)
                    }
                    composable("playlist",
                        arguments = listOf(navArgument("playlistId") { type = NavType.StringType })
                    ) {
                        val playlistId = it.arguments?.getString("playlistId")
                        PlaylistScreen(navController, playlistId = playlistId)
                    }
                    composable(
                        route = "playlist/{playlistId}",
                        arguments = listOf(navArgument("playlistId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val playlistId = backStackEntry.arguments?.getString("playlistId")
                        PlaylistScreen(navController, playlistId = playlistId )
                    }
                    composable("friends") {
                        FriendScreen(navController)
                    }
                    composable("settings") {
                        SettingScreen(navController)
                    }
                    composable("change_password") {
                        ChangePasswordScreen(navController, authViewModel)
                    }
                    composable("linked_accounts") {
                        LinkedAccountScreen(navController, authViewModel)
                    }
                    composable("player") {
                        ExpandedPlayer(navController, playbackViewModel)
                    }
                    composable(
                        route="profile/{userId}",
                        arguments = listOf(navArgument("userId") { type =
                            NavType.StringType })
                    ) {  backStackEntry ->
                        // TODO: Shouldnt fail this next userID
                        val userId = backStackEntry.arguments?.getString("userId") ?: "user"
                        ProfileScreen(
                            userId = userId,
                            profileViewModel = profileViewModel
                        )
                    }
                }
            }
            
            // Fixed bottom container with mini player and navbar
            // Show on main screens, hide on login and expanded player
            if (currentRoute != null && (
                        currentRoute.startsWith("main") ||
                                currentRoute.startsWith("map") ||
                                currentRoute.startsWith("friends") ||
                                currentRoute.startsWith("settings") ||
                                currentRoute.startsWith("profile") ||
                                currentRoute.startsWith("library")
                        )) {
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                ) {
                    // Mini player above navbar
                    MiniPlayer(
                        playbackViewModel = playbackViewModel,
                        onExpand = { navController.navigate("player") }
                    )
                    
                    // Bottom navigation bar
                    BottomNavBar(navController)
                }
            }
        } // Box
            } // SoraTheme
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.d(TAG, "onNewIntent() called")
        Log.d(TAG, "New intent: ${intent.data}")
        Log.d(TAG, "New intent action: ${intent.action}")
        setIntent(intent)

        // Handle Spotify callback with the stored authViewModel
        if (::authViewModel.isInitialized) {
            handleSpotifyCallback(intent, authViewModel)
        } else {
            Log.e(TAG, "AuthViewModel not initialized yet")
        }
    }

    private fun handleSpotifyCallback(intent: Intent?, authViewModel: AuthViewModel) {
        intent?.data?.let { uri ->
            if (uri.scheme == "com.example.sora" && uri.host == "callback") {
                Log.d(TAG, "Valid Spotify callback detected")
                Log.d(TAG, "Full URI: $uri")

                val authResponse = SpotifyAuthManager.handleAuthorizationResponse(intent)
                authResponse?.let { response ->
                    Log.d(TAG, "Valid auth response received, starting token exchange...")
                    CoroutineScope(Dispatchers.Main).launch {
                        try {
                            val tokenResult = SpotifyAuthManager.exchangeCodeForTokens(this@MainActivity, response)
                            tokenResult.onSuccess { tokenResponse ->
                                authViewModel.handleSpotifyAuthResult(
                                    tokenResponse.accessToken,
                                    tokenResponse.refreshToken,
                                    tokenResponse.expiresIn
                                )
                                Log.d(TAG, "Tokens successfully passed to AuthViewModel")
                            }.onFailure { exception ->
                                Log.e(TAG, "Token exchange failed: ${exception.message}")
                                authViewModel.setErrorMessage("Spotify connection failed: ${exception.message}")
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Exception during token exchange: ${e.message}")
                            Log.e(TAG, "Stack trace: ${e.stackTraceToString()}")
                            authViewModel.setErrorMessage("Error connecting to Spotify: ${e.message}")
                        }
                    }
                } ?: run {
                    Log.w(TAG, "No valid auth response from handleAuthorizationResponse")
                }
            } else {
                Log.w(TAG, "URI scheme/host mismatch - Expected: com.example.sora://callback, got: ${uri.scheme}://${uri.host}")
            }
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart() called")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume() called")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause() called")
    }


    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop() called")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy() called")
    }

}
