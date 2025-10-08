package com.example.sora.features


import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.net.toUri
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.ResponseTypeValues

object SpotifyAuthManager {

    private val serviceConfig = AuthorizationServiceConfiguration(
        ("https://accounts.spotify.com/authorize").toUri(),
        ("https://accounts.spotify.com/api/token").toUri()

    )

    private const val CLIENT_ID = "6918fb3c3d6245a9b88c3ad93fff7675"
    private val REDIRECT_URI = Uri.parse("com.example.sora://callback")

    private val scopes = arrayOf(
        "user-read-private",
        "user-read-email",
        "playlist-read-private"
    )

    fun getAuthorizationRequestIntent(context: Context): Intent {
        val request = AuthorizationRequest.Builder(
            serviceConfig,
            CLIENT_ID,
            ResponseTypeValues.CODE,
            REDIRECT_URI
        ).setScopes(*scopes)

        val authRequest = request.build()
        val authService = AuthorizationService(context)

        return authService.getAuthorizationRequestIntent(authRequest)
    }



}