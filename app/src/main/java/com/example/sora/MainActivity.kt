package com.example.sora

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.sora.auth.AuthViewModel
import com.example.sora.auth.Login
import com.example.sora.auth.Signup
import com.example.sora.features.SpotifyAuthManager
import com.example.sora.ui.MainScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private val TAG = "MainActivity"

class MainActivity : ComponentActivity() {
    private lateinit var authViewModel: AuthViewModel
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate(Bundle?) called")
        Log.d(TAG, "Intent: ${intent?.data}")
        Log.d(TAG, "Intent action: ${intent?.action}")
        
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            authViewModel = viewModel()

            // Handle Spotify callback intent
            LaunchedEffect(intent) {
                handleSpotifyCallback(intent, authViewModel)
            }

            Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize().padding(innerPadding)
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = "login"
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
        Log.d(TAG, "==================== HANDLE SPOTIFY CALLBACK ====================")
        intent?.data?.let { uri ->
            Log.d(TAG, "Callback URI received: $uri")
            Log.d(TAG, "URI scheme: ${uri.scheme}")
            Log.d(TAG, "URI host: ${uri.host}")
            Log.d(TAG, "URI path: ${uri.path}")
            Log.d(TAG, "URI query: ${uri.query}")
            
            if (uri.scheme == "com.example.sora" && uri.host == "callback") {
                Log.d(TAG, "Valid Spotify callback detected, processing...")
                val authResponse = SpotifyAuthManager.handleAuthorizationResponse(intent)
                
                authResponse?.let { response ->
                    Log.d(TAG, "Valid auth response received, starting token exchange...")
                    CoroutineScope(Dispatchers.Main).launch {
                        try {
                            val tokenResult = SpotifyAuthManager.exchangeCodeForTokens(this@MainActivity, response)
                            tokenResult.onSuccess { tokenResponse ->
                                Log.d(TAG, "==================== FINAL TOKEN RESPONSE ====================")
                                Log.d(TAG, "Access Token Length: ${tokenResponse.accessToken.length}")
                                Log.d(TAG, "Refresh Token Length: ${tokenResponse.refreshToken.length}")
                                Log.d(TAG, "Expires In: ${tokenResponse.expiresIn}")
                                Log.d(TAG, "Passing tokens to AuthViewModel...")
                                Log.d(TAG, "==============================================================")
                                
                                authViewModel.handleSpotifyAuthResult(
                                    tokenResponse.accessToken,
                                    tokenResponse.refreshToken,
                                    tokenResponse.expiresIn
                                )
                                Log.d(TAG, "Tokens successfully passed to AuthViewModel")
                            }.onFailure { exception ->
                                Log.e(TAG, "==================== TOKEN EXCHANGE FAILURE ====================")
                                Log.e(TAG, "Token exchange failed: ${exception.message}")
                                Log.e(TAG, "Exception: ${exception.stackTraceToString()}")
                                Log.e(TAG, "================================================================")
                                authViewModel.setErrorMessage("Spotify connection failed: ${exception.message}")
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "==================== EXCEPTION IN CALLBACK ====================")
                            Log.e(TAG, "Exception during token exchange: ${e.message}")
                            Log.e(TAG, "Stack trace: ${e.stackTraceToString()}")
                            Log.e(TAG, "===============================================================")
                            authViewModel.setErrorMessage("Error connecting to Spotify: ${e.message}")
                        }
                    }
                } ?: run {
                    Log.w(TAG, "No valid auth response from handleAuthorizationResponse")
                }
            } else {
                Log.w(TAG, "URI scheme/host mismatch - Expected: com.example.sora://callback")
            }
        } ?: run {
            Log.w(TAG, "Intent data is null - no callback URI")
        }
        Log.d(TAG, "=================================================================")
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
