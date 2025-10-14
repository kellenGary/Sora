package com.example.sora

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.sora.auth.AuthViewModel
import com.example.sora.auth.Login
import com.example.sora.auth.Signup
import com.example.sora.features.SpotifyAuthManager
import com.example.sora.ui.BottomNavBar
import com.example.sora.ui.MainScreen
import com.example.sora.ui.ProfileScreen
import com.example.sora.ui.settings.optionScreens.ChangePasswordScreen
import com.example.sora.ui.settings.SettingScreen
import com.example.sora.ui.settings.optionScreens.LinkedAccountScreen
import com.example.sora.viewmodel.ProfileViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val TAG = "MainActivity"

class MainActivity : ComponentActivity() {
    private lateinit var authViewModel: AuthViewModel
    private lateinit var profileViewModel: ProfileViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate(Bundle?) called")
        Log.d(TAG, "Intent: ${intent?.data}")
        Log.d(TAG, "Intent action: ${intent?.action}")

        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            authViewModel = viewModel()
            profileViewModel = viewModel()

            // Handle Spotify callback intent
            LaunchedEffect(intent) {
                handleSpotifyCallback(intent, authViewModel)
            }

            Scaffold(
                modifier = Modifier.fillMaxSize(),
                bottomBar = {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry?.destination?.route
                    if (currentRoute != null && (
                                currentRoute.startsWith("main") ||
                                        currentRoute.startsWith("map") ||
                                        currentRoute.startsWith("friends") ||
                                        currentRoute.startsWith("settings") ||
                                        currentRoute.startsWith("profile")
                                )) {
                        BottomNavBar(navController)
                    }
                }
            ) { innerPadding ->
                NavHost(
                    navController = navController,
                    startDestination = "login",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    composable("login") {
                        Login(navController, authViewModel)
                    }
                    composable("signup") {
                        Signup(navController, authViewModel)
                    }
                    composable("main") {
                        MainScreen(navController, authViewModel)
                    }
                    composable("map") {
                        // MapScreen()
                    }
                    composable("friends") {
                        // FriendsScreen()
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
                Log.d(TAG, "Valid Spotify callback detected, processing...")
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
                Log.w(TAG, "URI scheme/host mismatch - Expected: com.example.sora://callback")
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
