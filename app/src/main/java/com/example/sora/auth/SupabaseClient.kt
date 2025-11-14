package com.example.sora.auth

import android.content.Context
import androidx.startup.Initializer
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.FlowType
import io.github.jan.supabase.gotrue.SessionStatus
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import com.example.sora.BuildConfig
import android.util.Log
import io.github.jan.supabase.SupabaseClient as SupabaseClientType
import io.github.jan.supabase.annotations.SupabaseInternal


object SupabaseClient {
    lateinit var supabase: SupabaseClientType
        private set
    
    fun initialize(context: Context) {
        if (::supabase.isInitialized) {
            Log.d("SupabaseClient", "Already initialized")
            return
        }
        
        Log.d("SupabaseClient", "Initializing Supabase client...")
        supabase = createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_ANON_KEY
        ) {
            install(Auth) {
                // Configure OAuth providers
                scheme = "com.example.sora"
                host = "callback"
                flowType = FlowType.PKCE
                
                // Use persistent session manager - saves to encrypted SharedPreferences
                sessionManager = PersistentSessionManager(context)
                
                // Enable automatic token refresh (default is true, but explicit for clarity)
                autoLoadFromStorage = true
                autoSaveToStorage = true
                
                // Token will be refreshed automatically when it expires
                alwaysAutoRefresh = true
            }
            install(Postgrest)
            install(Storage)
        }
        
        Log.d("SupabaseClient", "Initialization complete")
        Log.d("SupabaseClient", "URL: ${BuildConfig.SUPABASE_URL}")
        Log.d("SupabaseClient", "Key starts with: ${BuildConfig.SUPABASE_ANON_KEY.take(10)}...")
        Log.d("SupabaseClient", "Auth configured with persistent session and auto-refresh")
    }
}
