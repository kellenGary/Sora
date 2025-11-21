package com.example.sora.library.playlists

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.sora.R
import com.example.sora.ui.BottomNavBar


@Composable
fun PlaylistScreen (
    navController: NavController,
    playlistViewModel: PlaylistViewModel = viewModel(),
    playlistId: String?
) {
    val uiState by playlistViewModel.uiState.collectAsState()
    LaunchedEffect(playlistId) {
        playlistViewModel.loadPlaylistDetails(playlistId)
    }
    val playlist = uiState.playlist
    Scaffold(
        bottomBar = {BottomNavBar(navController)}
    ) { innerPadding ->
        LazyColumn(
            contentPadding = innerPadding,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),

        ) {
            // --- Playlist header ---
            item {
                PlaylistHeader(playlist)
            }


            // --- Songs list ---
            playlist?.tracks?.total?.let {
                items(it) { trackItem ->
                    SongRow(playlist.tracks.items[trackItem].track, navController)
                }
            }
        }
    }
}

@Composable
fun PlaylistHeader(playlist: PlaylistDetailsResponse?) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(350.dp) // Give it height!
    ) {
        // 1. The Atmospheric Background
        AsyncImage(
            model = playlist?.images?.firstOrNull()?.url,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .blur(radius = 50.dp) // Requires Android 12+, or use a library/scrim
                .alpha(0.6f) // Fade it out so it's not too distracting
        )

        // Gradient overlay to blend into the black list below
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, MaterialTheme.colorScheme.background),
                        startY = 300f // Adjust gradient start
                    )
                )
        )

        // 2. The Actual Content
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(24.dp)
        ) {
            // The sharp cover image
            Card(
                elevation = CardDefaults.cardElevation(10.dp),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.size(160.dp)
            ) {
                AsyncImage(
                    model = playlist?.images?.firstOrNull()?.url,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Title and Emoji
            Row(verticalAlignment = Alignment.CenterVertically) {
                playlist?.name?.let {
                    Text(
                        text = it, // Replace with variable
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            Text(
                text = "Created by ${playlist?.owner?.display_name}",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(8.dp))


            Text(
                text = "${playlist?.description}",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
    }
}
@Composable
fun SongRow(song: Track, navController: NavController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* Play song */ }
            .padding(vertical = 8.dp, horizontal = 16.dp), // Give it breathing room
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Song Image
        AsyncImage(
            model = song.album.images?.firstOrNull()?.url,
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(8.dp)), // Soft corners look modern
            contentScale = ContentScale.Crop,
            contentDescription = null
        )

        Spacer(modifier = Modifier.width(16.dp))

        // Title & Artist
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = song.artists.firstOrNull()?.name ?: "",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                maxLines = 1
            )
        }

        // Duration & Options
        Text(
            text = formatMillis(song.duration_ms), // e.g., "4:42"
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )

        SongOptionMenu(
            onDetailsClick = {
                navController.navigate("song/${song.id}")
            }
        )
    }
}

@Composable
fun SongOptionMenu(
    onDetailsClick: () -> Unit // Pass your navigation logic here
) {
    // 1. State to control if the menu is open or closed
    var expanded by remember { mutableStateOf(false) }

    // 2. Box acts as the anchor for the menu
    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(
                painter = painterResource(id = R.drawable.more_vert_24),
                contentDescription = "Options",
                tint = Color.Gray
            )
        }

        // 3. The Menu itself
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false } // Close when clicking outside
        ) {
            DropdownMenuItem(
                text = { Text("View Song Details") },
                onClick = {
                    expanded = false // Close the menu first
                    onDetailsClick() // Then navigate
                }
            )
        }
    }
}

// Helper function to format duration in ms to mm:ss
fun formatMillis(ms: Int?): String {
    val totalSeconds = ms?.div(1000)
    val minutes = totalSeconds?.div(60)
    val seconds = totalSeconds?.rem(60)
    return String.format("%d:%02d", minutes, seconds)
}