package com.example.sora.auth

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import io.github.jan.supabase.gotrue.SessionManager
import io.github.jan.supabase.gotrue.user.UserSession
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class PersistentSessionManager(context: Context) : SessionManager {
    private val TAG = "PersistentSessionManager"
    
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    
    private val sharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "supabase_session_encrypted",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    
    private val json = Json {
        ignoreUnknownKeys = true
    }
    
    override suspend fun deleteSession() {
        Log.d(TAG, "Deleting session")
        sharedPreferences.edit().remove(KEY_SESSION).apply()
    }
    
    override suspend fun loadSession(): UserSession? {
        Log.d(TAG, "========== loadSession() called ==========")
        val sessionJson = sharedPreferences.getString(KEY_SESSION, null)
        
        if (sessionJson == null) {
            Log.d(TAG, "No saved session found in storage")
            return null
        }
        
        Log.d(TAG, "Found saved session, attempting to deserialize (${sessionJson.length} chars)")
        
        return try {
            val session = json.decodeFromString<UserSession>(sessionJson)
            Log.d(TAG, "✓ Successfully deserialized session")
            Log.d(TAG, "  User: ${session.user?.email} (${session.user?.id})")
            Log.d(TAG, "  Access token starts with: ${session.accessToken.take(20)}...")
            Log.d(TAG, "  Expires at: ${session.expiresAt}")
            Log.d(TAG, "========== loadSession() complete ==========")
            session
        } catch (e: Exception) {
            Log.e(TAG, "✗ Failed to deserialize session", e)
            Log.e(TAG, "Exception type: ${e.javaClass.simpleName}")
            Log.e(TAG, "Exception message: ${e.message}")
            Log.e(TAG, "Stack trace: ${e.stackTraceToString()}")
            
            // Clear corrupted session
            Log.w(TAG, "Clearing corrupted session data")
            sharedPreferences.edit().remove(KEY_SESSION).apply()
            
            Log.d(TAG, "========== loadSession() failed ==========")
            null
        }
    }
    
    override suspend fun saveSession(session: UserSession) {
        Log.d(TAG, "========== saveSession() called ==========")
        try {
            val sessionJson = json.encodeToString(session)
            Log.d(TAG, "Serialized session to JSON (${sessionJson.length} chars)")
            
            sharedPreferences.edit().putString(KEY_SESSION, sessionJson).apply()
            
            Log.d(TAG, "✓ Successfully saved session to storage")
            Log.d(TAG, "  User: ${session.user?.email} (${session.user?.id})")
            Log.d(TAG, "  Expires at: ${session.expiresAt}")
            Log.d(TAG, "========== saveSession() complete ==========")
        } catch (e: Exception) {
            Log.e(TAG, "✗ Failed to save session", e)
            Log.e(TAG, "Exception type: ${e.javaClass.simpleName}")
            Log.e(TAG, "Exception message: ${e.message}")
            Log.e(TAG, "========== saveSession() failed ==========")
        }
    }
    
    companion object {
        private const val KEY_SESSION = "supabase_session"
    }
}
