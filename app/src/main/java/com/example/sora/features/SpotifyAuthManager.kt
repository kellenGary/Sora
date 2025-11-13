package com.example.sora.features


import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.net.toUri
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.ResponseTypeValues
import net.openid.appauth.TokenRequest
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import com.example.sora.BuildConfig
import android.util.Log

data class SpotifyTokenResponse(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long
)

object SpotifyAuthManager {

    private const val TAG = "SpotifyAuthManager"

    private val serviceConfig = AuthorizationServiceConfiguration(
        ("https://accounts.spotify.com/authorize").toUri(),
        ("https://accounts.spotify.com/api/token").toUri()
    )

    private const val CLIENT_ID = BuildConfig.SPOTIFY_CLIENT_ID
    private val REDIRECT_URI = "com.example.sora://callback".toUri()

    private val scopes = arrayOf(
        "user-read-private",
        "user-read-email",
        "playlist-read-private",
        "user-read-playback-state",
        "user-modify-playback-state",
        "user-read-currently-playing"
    )
    
    // Store the last authorization request to retrieve the code_verifier
    private var lastAuthRequest: AuthorizationRequest? = null

    fun getAuthorizationRequestIntent(context: Context): Intent {
        Log.d(TAG, "Creating authorization request")
        Log.d(TAG, "Client ID: $CLIENT_ID")
        Log.d(TAG, "Redirect URI: $REDIRECT_URI")

        val request = AuthorizationRequest.Builder(
            serviceConfig,
            CLIENT_ID,
            ResponseTypeValues.CODE,
            REDIRECT_URI
        ).setScopes(*scopes)
            .setAdditionalParameters(mapOf("show_dialog" to "true"))

        val authRequest = request.build()
        
        // Store the request so we can use it later for token exchange
        lastAuthRequest = authRequest
        Log.d(TAG, "Stored authorization request for later use")
        Log.d(TAG, "Code verifier present: ${authRequest.codeVerifier != null}")
        
        val authService = AuthorizationService(context)

        return authService.getAuthorizationRequestIntent(authRequest)
    }

    suspend fun exchangeCodeForTokens(
        context: Context,
        authResponse: AuthorizationResponse
    ): Result<SpotifyTokenResponse> = suspendCancellableCoroutine { continuation ->
        Log.d(TAG, "Starting token exchange")
        Log.d(TAG, "Authorization code received: ${authResponse.authorizationCode != null}")
        Log.d(TAG, "Auth response state: ${authResponse.state}")

        val authService = AuthorizationService(context)

        // Create token exchange request without client secret (using PKCE only)
        val tokenRequest = authResponse.createTokenExchangeRequest()
        println("Token request: $tokenRequest")

        Log.d(TAG, "Making token request to: ${tokenRequest.configuration.tokenEndpoint}")

        authService.performTokenRequest(tokenRequest) { response, exception ->
            when {
                exception != null -> {
                    Log.e(TAG, "==================== TOKEN EXCHANGE FAILED ====================")
                    Log.e(TAG, "Exception type: ${exception::class.simpleName}")
                    Log.e(TAG, "Exception message: ${exception.message}")
                    Log.e(TAG, "Exception cause: ${exception.cause}")
                    Log.e(TAG, "Stack trace: ${exception.stackTraceToString()}")
                    Log.e(TAG, "===============================================================")
                    continuation.resumeWithException(exception)
                }
                response != null -> {
                    Log.d(TAG, "==================== TOKEN EXCHANGE SUCCESS ====================")
                    Log.d(TAG, "Access Token: ${response.accessToken}")
                    Log.d(TAG, "Access Token (first 20 chars): ${response.accessToken?.take(20)}...")
                    Log.d(TAG, "Refresh Token: ${response.refreshToken}")
                    Log.d(TAG, "Refresh Token (first 20 chars): ${response.refreshToken?.take(20)}...")
                    Log.d(TAG, "Token Type: ${response.tokenType}")
                    Log.d(TAG, "Scope: ${response.scope}")
                    Log.d(TAG, "Expires In (seconds): ${response.accessTokenExpirationTime}")
                    Log.d(TAG, "ID Token: ${response.idToken}")
                    Log.d(TAG, "Additional Parameters: ${response.additionalParameters}")
                    Log.d(TAG, "Request: ${response.request}")
                    Log.d(TAG, "===============================================================")

                    val tokenResponse = SpotifyTokenResponse(
                        accessToken = response.accessToken ?: "",
                        refreshToken = response.refreshToken ?: "",
                        expiresIn = response.accessTokenExpirationTime ?: 0L
                    )
                    
                    Log.d(TAG, "Created TokenResponse object:")
                    Log.d(TAG, "  - accessToken length: ${tokenResponse.accessToken.length}")
                    Log.d(TAG, "  - refreshToken length: ${tokenResponse.refreshToken.length}")
                    Log.d(TAG, "  - expiresIn: ${tokenResponse.expiresIn}")
                    
                    continuation.resume(Result.success(tokenResponse))
                }
                else -> {
                    Log.e(TAG, "==================== UNKNOWN ERROR ====================")
                    Log.e(TAG, "No response and no exception - unknown error")
                    Log.e(TAG, "=======================================================")
                    continuation.resumeWithException(Exception("Unknown error occurred"))
                }
            }
        }
    }

    fun handleAuthorizationResponse(intent: Intent): AuthorizationResponse? {
        Log.d(TAG, "==================== AUTHORIZATION RESPONSE ====================")
        Log.d(TAG, "Intent action: ${intent.action}")
        Log.d(TAG, "Intent data: ${intent.data}")
        Log.d(TAG, "Intent data scheme: ${intent.data?.scheme}")
        Log.d(TAG, "Intent data host: ${intent.data?.host}")
        Log.d(TAG, "Intent data path: ${intent.data?.path}")
        Log.d(TAG, "Intent data query: ${intent.data?.query}")
        Log.d(TAG, "Intent extras: ${intent.extras}")

        // First try the standard AppAuth way
        var response = AuthorizationResponse.fromIntent(intent)
        val error = net.openid.appauth.AuthorizationException.fromIntent(intent)
        
        Log.d(TAG, "Authorization response parsed (AppAuth): ${response != null}")
        Log.d(TAG, "Authorization error: ${error != null}")

        // If AppAuth couldn't parse it, manually parse from URI
        if (response == null && error == null) {
            Log.d(TAG, "AppAuth parsing failed, attempting manual parsing from URI...")
            val uri = intent.data
            if (uri != null) {
                val code = uri.getQueryParameter("code")
                val state = uri.getQueryParameter("state")
                
                Log.d(TAG, "Manual parsing - Code: ${code?.take(20)}...")
                Log.d(TAG, "Manual parsing - State: $state")
                
                when {
                    code != null && lastAuthRequest != null -> {
                        Log.d(TAG, "Creating AuthorizationResponse manually using stored request...")
                        Log.d(TAG, "Stored request has code_verifier: ${lastAuthRequest!!.codeVerifier != null}")
                        
                        // Build the response using the original authorization request
                        // This preserves the code_verifier for PKCE
                        response = AuthorizationResponse.Builder(lastAuthRequest!!)
                            .setAuthorizationCode(code)
                            .setState(state ?: lastAuthRequest!!.state)
                            .build()
                        
                        Log.d(TAG, "Manually created AuthorizationResponse successfully")
                        Log.d(TAG, "Response has code_verifier: ${response.request.codeVerifier != null}")
                    }
                    code != null && lastAuthRequest == null -> {
                        Log.e(TAG, "Cannot create response: lastAuthRequest is null")
                        Log.e(TAG, "This means the app was restarted between authorization and callback")
                    }
                    else -> {
                        Log.e(TAG, "No authorization code found in URI")
                    }
                }
            }
        }

        error?.let {
            Log.e(TAG, "==================== AUTHORIZATION ERROR ====================")
            Log.e(TAG, "Error code: ${it.code}")
            Log.e(TAG, "Error description: ${it.error}")
            Log.e(TAG, "Error details: ${it.errorDescription}")
            Log.e(TAG, "Error URI: ${it.errorUri}")
            Log.e(TAG, "============================================================")
        }

        response?.let {
            Log.d(TAG, "==================== AUTHORIZATION SUCCESS ====================")
            Log.d(TAG, "Authorization Code: ${it.authorizationCode}")
            Log.d(TAG, "State: ${it.state}")
            Log.d(TAG, "Token Type: ${it.tokenType}")
            Log.d(TAG, "Scope: ${it.scope}")
            Log.d(TAG, "Additional Parameters: ${it.additionalParameters}")
            Log.d(TAG, "Request: ${it.request}")
            if (it.authorizationCode != null) {
                Log.d(TAG, "Authorization code length: ${it.authorizationCode!!.length}")
                Log.d(TAG, "Authorization code (first 20 chars): ${it.authorizationCode!!.take(20)}...")
            }
            Log.d(TAG, "===============================================================")
        }
        
        if (response == null && error == null) {
            Log.w(TAG, "==================== NO RESPONSE OR ERROR ====================")
            Log.w(TAG, "Both response and error are null - callback may be malformed")
            Log.w(TAG, "==============================================================")
        }
        
        return response
    }
}