package com.example.sora

import android.app.Application
import android.util.Log
import com.example.sora.auth.SupabaseClient

class SoraApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Log.d("SoraApplication", "Application onCreate called")
        
        // Initialize Supabase client with session persistence
        SupabaseClient.initialize(this)
        
        Log.d("SoraApplication", "Application initialization complete")
    }
}
