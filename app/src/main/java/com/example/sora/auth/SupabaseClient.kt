package com.example.sora.auth

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest
import com.example.sora.BuildConfig
import android.util.Log

object SupabaseClient {
    val supabase = createSupabaseClient(
        supabaseUrl = BuildConfig.SUPABASE_URL,
        supabaseKey = BuildConfig.SUPABASE_ANON_KEY
    ) {
        install(Auth)
        install(Postgrest)
        //install other modules
    }

    init {
        Log.d("SupabaseClient", "Initializing with URL: ${BuildConfig.SUPABASE_URL}")
        Log.d("SupabaseClient", "Key starts with: ${BuildConfig.SUPABASE_ANON_KEY.take(10)}...")
    }
}
