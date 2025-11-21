package com.example.sora.map

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.android.gms.maps.model.*
import com.google.maps.android.compose.*

@Composable
fun MapScreen(
    navController: NavController? = null,
    mapViewModel: MapViewModel = viewModel()
) {
    val context = LocalContext.current
    val locationState by mapViewModel.locationState.collectAsState()
    val songLocations by mapViewModel.songLocations.collectAsState()
    var selectedSongs by remember { mutableStateOf<List<SongLocation>>(emptyList()) }
    
    // Group songs by location (considering songs within 50 meters as same location)
    fun getSongsAtLocation(location: LatLng, radius: Double = 50.0): List<SongLocation> {
        return songLocations.filter { song ->
            val results = FloatArray(1)
            android.location.Location.distanceBetween(
                song.location.latitude,
                song.location.longitude,
                location.latitude,
                location.longitude,
                results
            )
            results[0] <= radius
        }
    }

    // Permission launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        mapViewModel.onPermissionResult(granted)
        if (granted) {
            mapViewModel.startLocationUpdates(context)
        }
    }

    // Check permission and start location updates immediately
    LaunchedEffect(Unit) {
        println("MapScreen: Checking location permission...")
        mapViewModel.checkLocationPermission(context)
    }
    
    LaunchedEffect(locationState.hasLocationPermission) {
        if (locationState.hasLocationPermission) {
            println("MapScreen: Permission granted, starting location updates")
            mapViewModel.startLocationUpdates(context)
        }
    }

    // Camera position state - update only once when location is first obtained
    val cameraPositionState = rememberCameraPositionState()
    var hasInitializedCamera by remember { mutableStateOf(false) }
    
    // Update camera position only on first location update
    LaunchedEffect(locationState.currentLocation) {
        locationState.currentLocation?.let { location ->
            if (!hasInitializedCamera) {
                println("MapScreen: Setting initial camera position to: $location")
                cameraPositionState.position = CameraPosition.fromLatLngZoom(location, 16f)
                hasInitializedCamera = true
            }
        }
    }

    // UI settings for the map
    val uiSettings by remember {
        mutableStateOf(
            MapUiSettings(
                zoomControlsEnabled = false, // Hide +/- buttons
                zoomGesturesEnabled = true, // Keep pinch-to-zoom
                scrollGesturesEnabled = true,
                tiltGesturesEnabled = true,
                rotationGesturesEnabled = true,
                myLocationButtonEnabled = true
            )
        )
    }

    // Map properties - enable my location layer when permission is granted
    val mapProperties by remember(locationState.hasLocationPermission) {
        mutableStateOf(
            MapProperties(
                isMyLocationEnabled = locationState.hasLocationPermission,
                mapStyleOptions = MapStyleOptions.loadRawResourceStyle(
                    context,
                    com.example.sora.R.raw.map_style
                )
            )
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = mapProperties,
            uiSettings = uiSettings
        ) {
            // Display song locations as circles with uniform size
            val uniformRadius = 150.0 // Fixed radius in meters for all songs
            
            songLocations.forEach { songLocation ->
                // Generate colors for each circle
                val colors = listOf(
                    Color(0x88FF6B6B), // Red
                    Color(0x884ECDC4), // Blue
                    Color(0x88FFD93D), // Yellow
                    Color(0x88A8E6CF), // Green
                    Color(0x88FF8B94), // Pink
                    Color(0x88C7CEEA), // Purple
                    Color(0x88FFDAB9), // Orange
                    Color(0x8895E1D3)  // Teal
                )
                val colorIndex = songLocation.id.hashCode().mod(colors.size).let { 
                    if (it < 0) it + colors.size else it 
                }
                val circleColor = colors[colorIndex]
                
                Circle(
                    center = songLocation.location,
                    radius = uniformRadius,
                    fillColor = circleColor,
                    strokeColor = Color(0xAAFFFFFF),
                    strokeWidth = 3f,
                    clickable = true,
                    onClick = {
                        val songsAtLocation = getSongsAtLocation(songLocation.location, uniformRadius)
                        selectedSongs = songsAtLocation
                    }
                )
                
                // Add a marker at the center for better click detection
                Marker(
                    state = MarkerState(position = songLocation.location),
                    alpha = 0f,
                    onClick = {
                        val songsAtLocation = getSongsAtLocation(songLocation.location, uniformRadius)
                        selectedSongs = songsAtLocation
                        true
                    }
                )
            }
        }

        // Show song details popup when songs are selected
        if (selectedSongs.isNotEmpty()) {
            SongListPopup(
                songs = selectedSongs,
                onDismiss = { selectedSongs = emptyList() },
                onViewSong = { song ->
                    // Navigate to song details
                    navController?.navigate("song/${song.id}")
                    selectedSongs = emptyList()
                }
            )
        }

        // Show permission request button if needed
        if (!locationState.hasLocationPermission) {
            Button(
                onClick = {
                    locationPermissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                Text("Enable Location")
            }
        }
    }

    // Clean up location updates when screen is removed
    DisposableEffect(Unit) {
        onDispose {
            mapViewModel.stopLocationUpdates()
        }
    }
}
