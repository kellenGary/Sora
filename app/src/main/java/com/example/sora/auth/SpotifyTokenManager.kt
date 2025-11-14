package com.example.sora.auth

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class SpotifyTokenManager(context: Context) {
    private val TAG = "SpotifyTokenManager"
    
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    
    private val sharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "spotify_tokens_encrypted",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    
    companion object {
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_EXPIRES_AT = "expires_at"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_SUPABASE_PASSWORD = "supabase_password"
        
        @Volatile
        private var instance: SpotifyTokenManager? = null
        
        fun getInstance(context: Context): SpotifyTokenManager {
            return instance ?: synchronized(this) {
                instance ?: SpotifyTokenManager(context.applicationContext).also { instance = it }
            }
        }
    }
    
    fun saveTokens(
        accessToken: String,
        refreshToken: String,
        expiresIn: Long,
        userEmail: String? = null,
        userId: String? = null,
        supabasePassword: String? = null
    ) {
        val expiresAt = System.currentTimeMillis() + expiresIn
        
        sharedPreferences.edit().apply {
            putString(KEY_ACCESS_TOKEN, accessToken)
            putString(KEY_REFRESH_TOKEN, refreshToken)
            putLong(KEY_EXPIRES_AT, expiresAt)
            userEmail?.let { putString(KEY_USER_EMAIL, it) }
            userId?.let { putString(KEY_USER_ID, it) }
            supabasePassword?.let { putString(KEY_SUPABASE_PASSWORD, it) }
            apply()
        }
        
        Log.d(TAG, "Tokens saved to local storage (expires at: $expiresAt)")
    }
    
    fun getAccessToken(): String? {
        val token = sharedPreferences.getString(KEY_ACCESS_TOKEN, null)
        if (token != null && isTokenExpired()) {
            Log.w(TAG, "Access token is expired")
            return null
        }
        return token
    }
    
    fun getRefreshToken(): String? {
        return sharedPreferences.getString(KEY_REFRESH_TOKEN, null)
    }
    
    fun getExpiresAt(): Long {
        return sharedPreferences.getLong(KEY_EXPIRES_AT, 0L)
    }
    
    fun getUserEmail(): String? {
        return sharedPreferences.getString(KEY_USER_EMAIL, null)
    }
    
    fun getUserId(): String? {
        return sharedPreferences.getString(KEY_USER_ID, null)
    }
    
    fun getSupabasePassword(): String? {
        return sharedPreferences.getString(KEY_SUPABASE_PASSWORD, null)
    }
    
    fun isTokenExpired(): Boolean {
        val expiresAt = getExpiresAt()
        val now = System.currentTimeMillis()
        // Consider token expired 5 minutes before actual expiry
        val buffer = 5 * 60 * 1000
        return expiresAt <= (now + buffer)
    }
    
    fun hasValidTokens(): Boolean {
        val hasTokens = getAccessToken() != null && getRefreshToken() != null
        val notExpired = !isTokenExpired()
        return hasTokens && notExpired
    }
    
    fun hasStoredCredentials(): Boolean {
        return getRefreshToken() != null
    }
    
    fun clearTokens() {
        sharedPreferences.edit().clear().apply()
        Log.d(TAG, "All tokens cleared from local storage")
    }
}
