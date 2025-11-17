package com.example.sora.map

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.*

@Composable
fun MiniMapScreen(
    navController: NavController? = null,
    mapViewModel: MapViewModel = viewModel()
) {
    val context = LocalContext.current
    val locationState by mapViewModel.locationState.collectAsState()
    val songLocations by mapViewModel.songLocations.collectAsState()

    // Check permission and start location updates immediately (same as MapScreen)
    LaunchedEffect(Unit) {
        println("MiniMapScreen: Checking location permission...")
        mapViewModel.checkLocationPermission(context)
    }
    
    LaunchedEffect(locationState.hasLocationPermission) {
        if (locationState.hasLocationPermission) {
            println("MiniMapScreen: Permission granted, starting location updates")
            mapViewModel.startLocationUpdates(context)
        }
    }

    // Camera position state - update instantly to user location (no animation)
    val cameraPositionState = rememberCameraPositionState()
    
    // Update camera position instantly when user location changes
    LaunchedEffect(locationState.currentLocation) {
        locationState.currentLocation?.let { location ->
            println("MiniMapScreen: Setting camera position to: $location")
            cameraPositionState.position = CameraPosition.fromLatLngZoom(location, 16f)
        }
    }

    // Debug logging (same as MapScreen)
    LaunchedEffect(songLocations) {
        println("MiniMapScreen: songLocations count = ${songLocations.size}")
        songLocations.forEach { song ->
            println("Song: ${song.songTitle} at ${song.location}")
        }
    }

    // UI settings for the mini map (no controls, not scrollable)
    val uiSettings = remember {
        MapUiSettings(
            zoomControlsEnabled = false,
            zoomGesturesEnabled = false,
            scrollGesturesEnabled = false, // Not scrollable
            tiltGesturesEnabled = false,
            rotationGesturesEnabled = false,
            myLocationButtonEnabled = false
        )
    }

    // Map properties - same as MapScreen with dark style
    val mapProperties = remember(locationState.hasLocationPermission) {
        MapProperties(
            isMyLocationEnabled = locationState.hasLocationPermission,
            mapStyleOptions = MapStyleOptions.loadRawResourceStyle(
                context,
                com.example.sora.R.raw.map_style
            )
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
            .clip(RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp) // Ensure Box matches Card height
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = mapProperties,
                uiSettings = uiSettings
            ) {
                // Display song locations as circles - same as MapScreen
                songLocations.forEach { songLocation ->
                    // Generate colors for each circle - same colors as MapScreen
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
                        radius = songLocation.radiusMeters,
                        fillColor = circleColor,
                        strokeColor = Color(0xAAFFFFFF),
                        strokeWidth = 3f // Same stroke width as MapScreen
                    )
                }
            }
            
            // Transparent clickable overlay to capture all clicks
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable {
                        navController?.navigate("map")
                    }
            )
        }
    }

    // Clean up location updates when screen is removed (same as MapScreen)
    DisposableEffect(Unit) {
        onDispose {
            mapViewModel.stopLocationUpdates()
        }
    }
}
