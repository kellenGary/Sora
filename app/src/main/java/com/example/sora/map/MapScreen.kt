package com.example.sora.map

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.sora.auth.AuthViewModel
import com.google.android.gms.maps.CameraUpdateFactory
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
    var selectedSong by remember { mutableStateOf<SongLocation?>(null) }

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

    // Camera position state - update instantly to user location (no animation)
    val cameraPositionState = rememberCameraPositionState()
    
    // Update camera position instantly when user location changes
    LaunchedEffect(locationState.currentLocation) {
        locationState.currentLocation?.let { location ->
            println("MapScreen: Setting camera position to: $location")
            cameraPositionState.position = CameraPosition.fromLatLngZoom(location, 13f)
        }
    }

    // UI settings for the map
    val uiSettings by remember {
        mutableStateOf(
            MapUiSettings(
                zoomControlsEnabled = false,
                zoomGesturesEnabled = false,
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

    // Debug logging
    LaunchedEffect(songLocations) {
        println("MapScreen: songLocations count = ${songLocations.size}")
        songLocations.forEach { song ->
            println("Song: ${song.songTitle} at ${song.location}")
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = mapProperties,
            uiSettings = uiSettings
        ) {
            // Display song locations as circles
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
                    radius = songLocation.radiusMeters,
                    fillColor = circleColor,
                    strokeColor = Color(0xAAFFFFFF),
                    strokeWidth = 3f,
                    onClick = {
                        selectedSong = songLocation
                    }
                )
                
                // Add an invisible small marker at the center for click detection
                Marker(
                    state = MarkerState(position = songLocation.location),
                    alpha = 0f, // Make it completely transparent
                    onClick = {
                        selectedSong = songLocation
                        true // Consume the click event
                    }
                )
            }
        }

        // Show song details popup when a song is selected
        selectedSong?.let { song ->
            SongDetailsPopup(
                song = song,
                onDismiss = { selectedSong = null },
                onViewSong = {
                    // Navigate to song details
                    navController?.navigate("song/${song.id}")
                    selectedSong = null
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

@Composable
fun SongDetailsPopup(
    song: SongLocation,
    onDismiss: () -> Unit,
    onViewSong: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.BottomCenter
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, end = 12.dp, bottom = 132.dp) // Position above navbar + mini player
                .clickable(enabled = false) { }, // Prevent click propagation to background
            shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Album art
                    if (song.albumArtUrl != null) {
                        AsyncImage(
                            model = song.albumArtUrl,
                            contentDescription = "Album art",
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        // Placeholder if no album art
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "♪",
                                style = MaterialTheme.typography.headlineLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    // Song info
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = song.songTitle,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = song.artist,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // View Song button
                Button(
                    onClick = onViewSong,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Text("View Song Details")
                }
            }
        }
    }
}

// Helper function to format radius for display
private fun formatRadius(radiusMeters: Double): String {
    return if (radiusMeters < 1000) {
        "${radiusMeters.toInt()}m radius"
    } else {
        String.format("%.1fkm radius", radiusMeters / 1000)
    }
}
