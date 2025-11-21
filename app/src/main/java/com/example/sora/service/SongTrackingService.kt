package com.example.sora.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.sora.MainActivity
import com.example.sora.R
import com.example.sora.auth.AuthRepository
import com.example.sora.auth.SpotifyTokenManager
import com.example.sora.auth.SpotifyTokenRefresher
import com.example.sora.auth.SupabaseClient
import com.example.sora.data.repository.SongTrackingRepository
import com.example.sora.data.repository.UserRepository
import com.example.sora.location.LocationProvider
import com.example.sora.playback.SpotifyPlaybackManager
import com.example.sora.playback.SpotifyTrack
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class SongTrackingService : Service() {
    private val TAG = "SongTrackingService"
    private val CHANNEL_ID = "song_tracking_channel"
    private val NOTIFICATION_ID = 1
    
    private val serviceScope = CoroutineScope(Dispatchers.Default + Job())
    private lateinit var authRepository: AuthRepository
    private val songTrackingRepository = SongTrackingRepository()
    private val userRepository = UserRepository()
    private lateinit var locationProvider: LocationProvider
    private lateinit var tokenManager: SpotifyTokenManager
    
    private var currentTrackId: String? = null
    private var pollingJob: Job? = null
    private var isUserActive = false

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "========== Service created ==========")
        authRepository = AuthRepository(this)
        locationProvider = LocationProvider(this)
        tokenManager = SpotifyTokenManager.getInstance(this)
        
        // Debug: Check stored credentials
        val storedEmail = tokenManager.getUserEmail()
        val storedPassword = tokenManager.getSupabasePassword()
        val storedUserId = tokenManager.getUserId()
        Log.d(TAG, "Stored credentials - email: ${storedEmail != null}, password: ${storedPassword != null}, userId: ${storedUserId != null}")
        if (storedUserId != null) {
            Log.d(TAG, "Stored userId: $storedUserId")
        }
        
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification("Initializing..."))
        
        // Restore Supabase session for database access
        serviceScope.launch {
            // Give Supabase more time to initialize and load session
            delay(2000)
            
            Log.d(TAG, "Attempting to restore Supabase session...")
            val authenticated = restoreSupabaseSession()
            
            if (authenticated) {
                Log.d(TAG, "✓ Supabase session active with valid user, starting monitoring")
                updateNotification("Monitoring your music...")
            } else {
                // Even without active session, we can still track using userId
                val userId = tokenManager.getUserId()
                if (userId != null) {
                    Log.w(TAG, "⚠ No active Supabase session, but userId available - will track with userId only")
                    Log.w(TAG, "Note: This requires RLS to be disabled on tables or service role key")
                    updateNotification("Monitoring (limited authentication)...")
                } else {
                    Log.e(TAG, "✗ No session and no userId - cannot track songs")
                    updateNotification("Authentication required - please open app")
                    // Stop the service since we can't track anything
                    stopSelf()
                    return@launch
                }
            }
            
            startMonitoring()
        }
    }
    
    private suspend fun restoreSupabaseSession(): Boolean {
        return try {
            Log.d(TAG, "========== STARTING SESSION RESTORATION ==========")
            
            // Wait for session to load from storage - Supabase automatically loads persisted sessions
            val sessionStatus = SupabaseClient.supabase.auth.sessionStatus
            var currentStatus = sessionStatus.value
            Log.d(TAG, "Initial session status: $currentStatus")
            
            // If loading, wait for it to complete (give more time than before)
            if (currentStatus is io.github.jan.supabase.gotrue.SessionStatus.LoadingFromStorage) {
                Log.d(TAG, "Session is loading from storage, waiting...")
                var attempts = 0
                val maxWaitAttempts = 20 // Wait up to 10 seconds
                while (currentStatus is io.github.jan.supabase.gotrue.SessionStatus.LoadingFromStorage && attempts < maxWaitAttempts) {
                    delay(500)
                    currentStatus = sessionStatus.value
                    attempts++
                    Log.d(TAG, "Waiting for session load... attempt $attempts/$maxWaitAttempts, status: $currentStatus")
                }
                
                // If still loading after max attempts, something is wrong
                if (currentStatus is io.github.jan.supabase.gotrue.SessionStatus.LoadingFromStorage) {
                    Log.e(TAG, "⚠️ Session STUCK in LoadingFromStorage state after ${maxWaitAttempts * 500}ms")
                    Log.e(TAG, "This indicates PersistentSessionManager.loadSession() is failing silently")
                    Log.e(TAG, "Check PersistentSessionManager logs for deserialization errors")
                    
                    // Try manual sign-in as fallback
                    try {
                        val email = tokenManager.getUserEmail()
                        val password = tokenManager.getSupabasePassword()
                        
                        if (email != null && password != null) {
                            Log.w(TAG, "Attempting manual sign-in as fallback...")
                            SupabaseClient.supabase.auth.signInWith(io.github.jan.supabase.gotrue.providers.builtin.Email) {
                                this.email = email
                                this.password = password
                            }
                            
                            delay(1000)
                            val manualUser = SupabaseClient.supabase.auth.currentUserOrNull()
                            if (manualUser != null) {
                                Log.d(TAG, "✓ Manual sign-in successful: ${manualUser.email}")
                                Log.d(TAG, "========== SESSION RESTORATION COMPLETE (MANUAL) ==========")
                                return true
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Manual sign-in also failed: ${e.message}")
                    }
                }
            }
            
            // Check if session was loaded from storage
            val currentUser = SupabaseClient.supabase.auth.currentUserOrNull()
            Log.d(TAG, "After waiting - session status: $currentStatus")
            Log.d(TAG, "Current user: ${if (currentUser != null) "${currentUser.email} (${currentUser.id})" else "null"}")
            
            if (currentUser != null) {
                Log.d(TAG, "✓ Session successfully loaded from storage - user authenticated")
                Log.d(TAG, "========== SESSION RESTORATION COMPLETE ==========")
                return true
            }
            
            // If no session was loaded, check if this is expected
            val userId = tokenManager.getUserId()
            if (userId == null) {
                Log.e(TAG, "No persisted session found and no userId in storage - user needs to log in")
                Log.d(TAG, "========== SESSION RESTORATION FAILED ==========")
                return false
            }
            
            Log.w(TAG, "No session loaded but userId exists in storage - session may have expired")
            Log.w(TAG, "Note: User will need to re-authenticate in the app")
            Log.w(TAG, "For now, we'll track songs with userId only (requires RLS to be disabled)")
            Log.d(TAG, "========== SESSION RESTORATION INCOMPLETE ==========")
            
            // Return false to indicate no active session, but don't crash the service
            // The trackSong method will use the userId from storage
            false
        } catch (e: Exception) {
            Log.e(TAG, "Exception during session restoration: ${e.message}", e)
            Log.e(TAG, "Stack trace: ${e.stackTraceToString()}")
            Log.d(TAG, "========== SESSION RESTORATION FAILED ==========")
            false
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service started")
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startMonitoring() {
        pollingJob?.cancel()
        pollingJob = serviceScope.launch {
            // Give Supabase session time to fully load before first check
            Log.d(TAG, "Monitoring started - waiting 5 seconds for session to stabilize...")
            delay(5000)
            
            var retryCount = 0
            val maxRetries = 3
            
            while (isActive) {
                try {
                    val hasToken = checkAndTrackCurrentSong()
                    
                    if (!hasToken && retryCount < maxRetries) {
                        // Token not available yet, retry sooner
                        retryCount++
                        Log.d(TAG, "No token available, retry $retryCount/$maxRetries in 5 seconds")
                        delay(5000)
                    } else if (hasToken) {
                        // Token available, normal operation
                        retryCount = 0
                        delay(10000) // Check every 10 seconds
                    } else {
                        // Max retries reached, wait longer
                        delay(30000) // Check every 30 seconds
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error in monitoring loop", e)
                    delay(15000) // Wait longer on error
                }
            }
        }
    }

    private suspend fun checkAndTrackCurrentSong(): Boolean {
        // Get valid access token (refreshes if needed)
        val accessToken = SpotifyTokenRefresher.getValidAccessToken(this)
        if (accessToken == null) {
            Log.w(TAG, "No valid Spotify access token available yet, will retry soon")
            updateNotification("Initializing Spotify connection...")
            return false
        }

        Log.d(TAG, "Valid access token obtained, checking playback...")
        val result = SpotifyPlaybackManager.getCurrentPlayback(accessToken)
        result.onSuccess { playbackState ->
            val userId = tokenManager.getUserId()

            if (playbackState?.item != null && playbackState.isPlaying) {
                val track = playbackState.item
                
                // Update active status if needed
                if (!isUserActive && userId != null) {
                    serviceScope.launch {
                        userRepository.updateUserActiveStatus(true, userId)
                        isUserActive = true
                        Log.d(TAG, "User marked as ACTIVE")
                    }
                }

                // Check if this is a new song
                if (currentTrackId != track.id) {
                    Log.d(TAG, "New song detected: ${track.name} by ${track.artists.firstOrNull()?.name}")
                    currentTrackId = track.id
                    
                    // Update notification with current song
                    updateNotification("Now tracking: ${track.name}")
                    
                    // Track the song with location
                    trackSong(track)
                }
            } else {
                // No playback or paused
                if (isUserActive && userId != null) {
                    serviceScope.launch {
                        userRepository.updateUserActiveStatus(false, userId)
                        isUserActive = false
                        Log.d(TAG, "User marked as INACTIVE (paused/stopped)")
                    }
                }

                if (playbackState == null) {
                    updateNotification("No active playback")
                } else if (!playbackState.isPlaying) {
                    updateNotification("Playback paused")
                }
            }
        }.onFailure { exception ->
            Log.e(TAG, "Failed to get playback state", exception)
            updateNotification("Monitoring (connection issue)")
        }
        
        return true // Token was available
    }

    private fun trackSong(track: SpotifyTrack) {
        serviceScope.launch {
            try {
                val location = locationProvider.getCurrentLocation()
                if (location == null) {
                    Log.w(TAG, "Could not get location for song: ${track.name}")
                    return@launch
                }

                // Get userId from local storage
                val userId = tokenManager.getUserId()
                if (userId == null) {
                    Log.e(TAG, "No user ID available in local storage")
                    return@launch
                }

                Log.d(TAG, "Tracking song at location: ${location.latitude}, ${location.longitude} for userId: $userId")
                
                // Pass userId explicitly - more reliable than relying on session
                val result = songTrackingRepository.trackSongListen(
                    spotifyTrack = track,
                    latitude = location.latitude,
                    longitude = location.longitude,
                    userId = userId
                )

                result.onSuccess {
                    Log.d(TAG, "Successfully tracked song: ${track.name}")
                }.onFailure { exception ->
                    Log.e(TAG, "Failed to track song: ${track.name}", exception)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error tracking song", e)
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Song Tracking",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Tracks your Spotify listening with location"
            }
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(contentText: String): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Sora Music Tracking")
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(contentText: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, createNotification(contentText))
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service destroyed")
        
        val userId = tokenManager.getUserId()
        if (userId != null) {
            CoroutineScope(Dispatchers.IO).launch {
                userRepository.updateUserActiveStatus(false, userId)
                Log.d(TAG, "User marked as INACTIVE (service destroy)")
            }
        }
        
        pollingJob?.cancel()
        serviceScope.cancel()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        Log.d(TAG, "Task removed (app swiped away)")

        val userId = tokenManager.getUserId()
        if (userId != null) {
            CoroutineScope(Dispatchers.IO).launch {
                userRepository.updateUserActiveStatus(false, userId)
                Log.d(TAG, "User marked as INACTIVE (task removed)")
            }
        }
        stopSelf()
    }

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, SongTrackingService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, SongTrackingService::class.java)
            context.stopService(intent)
        }
    }
}
