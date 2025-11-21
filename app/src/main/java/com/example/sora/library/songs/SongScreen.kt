package com.example.sora.library.songs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Tag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage

@Composable
fun SongScreen(
    navController: NavController? = null,
    songViewModel: SongViewModel = viewModel(),
    songId: String? = null
) {
    val uiState by songViewModel.uiState.collectAsState()

    LaunchedEffect(songId) {
        songViewModel.loadSongDetails(songId)
    }

    val song = uiState.song

    // Only show content if song data is loaded
    if (song != null) {
        SongDetailsContent(song)
    } else {
        // Optional: A simple loading indicator while fetching data
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color.White)
        }
    }
}

@Composable
fun SongDetailsContent(song: SpotifyTrackResponse) {
    val imageUrl = song.album.images?.firstOrNull()?.url
    val artistName = song.album.artists?.firstOrNull()?.name ?: "Unknown Artist"

    Box(modifier = Modifier.fillMaxSize()) {
        // --- LAYER 1: The Atmospheric Background ---
        // 1. The blurred image filling the screen
        AsyncImage(
            model = imageUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                // Note: .blur requires Android 12+. If on older API, this line is ignored safely.
                .blur(radius = 50.dp)
                .alpha(0.6f)
        )

        // 2. A dark gradient scrim so white text is readable
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Black.copy(alpha = 0.3f), Color(0xFF121212)),
                        startY = 100f
                    )
                )
        )

        // --- LAYER 2: The Content ScrollView ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()) // Replaces LazyColumn for this layout
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp)) // Top padding

            // 1. Hero Image with Shadow
            Card(
                modifier = Modifier.size(260.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
            ) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Album Cover",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 2. Main Titles
            Text(
                text = song.name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = artistName,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                color = Color.LightGray
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 3. The Stats Grid Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    icon = Icons.Rounded.Star,
                    label = "Popularity",
                    value = "${song.popularity ?: 0}"
                )
                StatItem(
                    icon = Icons.Rounded.Tag,
                    label = "Track",
                    value = "#${song.track_number}"
                )
                StatItem(
                    icon = Icons.Rounded.Schedule,
                    label = "Duration",
                    value = formatMillis(song.duration_ms)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(24.dp))

            // 4. Secondary Album Details
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "ALBUM DETAILS",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                DetailRow(
                    icon = Icons.Rounded.Album,
                    title = "Album",
                    value = song.album.name ?: "Unknown"
                )
                Spacer(modifier = Modifier.height(16.dp))
                DetailRow(
                    icon = Icons.Rounded.CalendarToday,
                    title = "Released",
                    value = song.album.release_date ?: "Unknown"
                )
            }

            Spacer(modifier = Modifier.height(48.dp)) // Bottom padding
        }
    }
}

// --- Helper Composables ---

@Composable
fun StatItem(icon: ImageVector, label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = Color.White)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
    }
}

@Composable
fun DetailRow(icon: ImageVector, title: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.Gray,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}

// Ensure this matches your existing helper function logic
fun formatMillis(ms: Int?): String {
    val totalSeconds = ms?.div(1000)
    val minutes = totalSeconds?.div(60)
    val seconds = totalSeconds?.rem(60)
    return String.format("%d:%02d", minutes, seconds)
}