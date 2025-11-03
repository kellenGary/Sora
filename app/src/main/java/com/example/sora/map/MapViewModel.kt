package com.example.sora.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class LocationState(
    val currentLocation: LatLng? = null,
    val hasLocationPermission: Boolean = false,
    val isLoading: Boolean = false
)

class MapViewModel : ViewModel() {
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
                
                // Generate dummy song locations with last known location
                if (_songLocations.value.isEmpty()) {
                    println("MapViewModel: Generating songs from last known location...")
                    generateDummySongLocations(userLocation)
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
                    
                    // Generate dummy song locations when we first get the user's location
                    if (_songLocations.value.isEmpty()) {
                        println("MapViewModel: Song locations empty, generating...")
                        generateDummySongLocations(userLocation)
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

    fun generateDummySongLocations(centerLocation: LatLng) {
        println("MapViewModel: Generating dummy song locations at $centerLocation")
        println("MapViewModel: Current song locations before: ${_songLocations.value.size}")
        val dummyLocations = SongLocationGenerator.generateDummyLocations(
            centerLocation = centerLocation,
            count = 15,
            maxDistanceMeters = 5000.0
        )
        println("MapViewModel: Generated ${dummyLocations.size} locations")
        dummyLocations.forEach { 
            println("MapViewModel: Song location - ${it.songTitle} at ${it.location}, radius ${it.radiusMeters}m")
        }
        _songLocations.value = dummyLocations
        println("MapViewModel: Current song locations after: ${_songLocations.value.size}")
        println("MapViewModel: StateFlow value: ${songLocations.value.size}")
    }

    override fun onCleared() {
        super.onCleared()
        stopLocationUpdates()
    }
}