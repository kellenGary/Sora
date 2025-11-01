package com.example.sora.auth

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import com.example.sora.BuildConfig
import android.util.Log

object SupabaseClient {
    val supabase = createSupabaseClient(
        supabaseUrl = BuildConfig.SUPABASE_URL,
        supabaseKey = BuildConfig.SUPABASE_ANON_KEY
    ) {
        install(Auth) {
            // Configure OAuth providers
            // This enables deep link handling for OAuth callbacks
            scheme = "com.example.sora"
            host = "callback"
        }
        install(Postgrest)
        install(Storage)
        //install other modules
    }

    init {
        Log.d("SupabaseClient", "Initializing with URL: ${BuildConfig.SUPABASE_URL}")
        Log.d("SupabaseClient", "Key starts with: ${BuildConfig.SUPABASE_ANON_KEY.take(10)}...")
        Log.d("SupabaseClient", "OAuth redirect configured: com.example.sora://callback")
    }
}
