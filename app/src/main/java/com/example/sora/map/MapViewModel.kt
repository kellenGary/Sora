package com.example.sora.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Looper
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sora.data.repository.MapRepository
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LocationState(
    val currentLocation: LatLng? = null,
    val hasLocationPermission: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class MapViewModel : ViewModel() {
    private val mapRepository = MapRepository()
    
    private val _locationState = MutableStateFlow(LocationState())
    val locationState: StateFlow<LocationState> = _locationState.asStateFlow()

    private val _songLocations = MutableStateFlow<List<SongLocation>>(emptyList())
    val songLocations: StateFlow<List<SongLocation>> = _songLocations.asStateFlow()

    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var locationCallback: LocationCallback? = null

    fun checkLocationPermission(context: Context) {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        _locationState.value = _locationState.value.copy(hasLocationPermission = hasPermission)
    }

    fun onPermissionResult(granted: Boolean) {
        _locationState.value = _locationState.value.copy(hasLocationPermission = granted)
    }

    @SuppressLint("MissingPermission")
    fun startLocationUpdates(context: Context) {
        if (!_locationState.value.hasLocationPermission) return

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        
        // Try to get last known location first for immediate display
        fusedLocationClient?.lastLocation?.addOnSuccessListener { location ->
            location?.let {
                val userLocation = LatLng(it.latitude, it.longitude)
                println("MapViewModel: Got last known location: $userLocation")
                _locationState.value = _locationState.value.copy(
                    currentLocation = userLocation,
                    isLoading = false
                )
                
                // Fetch friends' listening history when we get location
                if (_songLocations.value.isEmpty()) {
                    println("MapViewModel: Fetching friends' listening history...")
                    fetchFriendsListeningHistory()
                }
            }
        }
        
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            10000L // 10 seconds
        ).apply {
            setMinUpdateIntervalMillis(5000L) // 5 seconds
        }.build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    val userLocation = LatLng(location.latitude, location.longitude)
                    println("MapViewModel: Got location update: $userLocation")
                    _locationState.value = _locationState.value.copy(
                        currentLocation = userLocation,
                        isLoading = false
                    )
                    
                    // Fetch friends' listening history when we first get the user's location
                    if (_songLocations.value.isEmpty()) {
                        println("MapViewModel: Song locations empty, fetching...")
                        fetchFriendsListeningHistory()
                    } else {
                        println("MapViewModel: Already have ${_songLocations.value.size} song locations")
                    }
                }
            }
        }

        _locationState.value = _locationState.value.copy(isLoading = true)

        fusedLocationClient?.requestLocationUpdates(
            locationRequest,
            locationCallback as LocationCallback,
            Looper.getMainLooper()
        )
    }

    fun stopLocationUpdates() {
        locationCallback?.let {
            fusedLocationClient?.removeLocationUpdates(it)
        }
    }

    fun fetchFriendsListeningHistory() {
        println("MapViewModel: Fetching friends' listening history from Supabase")
        viewModelScope.launch {
            _locationState.value = _locationState.value.copy(isLoading = true, errorMessage = null)
            
            val result = mapRepository.getFriendsListeningHistory()
            
            result.onSuccess { locations ->
                println("MapViewModel: Successfully fetched ${locations.size} song locations")
                locations.forEach { location ->
                    println("MapViewModel: Song location - ${location.songTitle} by ${location.artist} at ${location.location}")
                }
                _songLocations.value = locations
                _locationState.value = _locationState.value.copy(isLoading = false)
            }.onFailure { error ->
                println("MapViewModel: Error fetching song locations: ${error.message}")
                _locationState.value = _locationState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to load friends' music: ${error.message}"
                )
            }
        }
    }
    
    fun refreshListeningHistory() {
        fetchFriendsListeningHistory()
    }

    override fun onCleared() {
        super.onCleared()
        stopLocationUpdates()
    }
}