package com.example.sora.library.playlists

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage


@Composable
fun PlaylistScreen (
    navController: NavController? = null,
    playlistViewModel: PlaylistViewModel = viewModel(),
    playlistId: String?
) {
    val uiState by playlistViewModel.uiState.collectAsState()
    LaunchedEffect(playlistId) {
        playlistViewModel.loadPlaylistDetails(playlistId)
    }
    val playlist = uiState.playlist



    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // --- Playlist header ---
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Playlist cover
                AsyncImage(
                    model = playlist?.images?.firstOrNull()?.url,
                    contentDescription = "Playlist Cover",
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Gray),
                    contentScale = ContentScale.Crop
                )

                // Spacer between image and text
                Spacer(modifier = Modifier.width(12.dp))

                // Name and description
                Column(
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = playlist?.name ?: "",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = playlist?.description ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 2
                    )
                }
            }
        }

        // --- Songs list ---
        playlist?.tracks?.total?.let {
            items(it) { trackItem ->
                SongRow(playlist.tracks.items[trackItem].track, modifier = Modifier.clickable {
                    navController?.navigate("song/${playlist.tracks.items[trackItem].track.id}")
                })
            }
        }
    }
}

@Composable
fun SongRow(song: Track?, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Album cover
        AsyncImage(
            model = song?.album?.images?.firstOrNull()?.url,
            contentDescription = "Album Cover",
            modifier = Modifier
                .size(50.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color.Gray),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Song name and artist
        Column(
            modifier = Modifier.weight(1f)
        ) {
            song?.name?.let {
                Text(
                    text = it,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
            }
            song?.artists?.joinToString(", ") { it.name ?: "" }?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    maxLines = 1
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Song duration
        Text(
            text = formatMillis(song?.duration_ms),
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
    }
}

// Helper function to format duration in ms to mm:ss
fun formatMillis(ms: Int?): String {
    val totalSeconds = ms?.div(1000)
    val minutes = totalSeconds?.div(60)
    val seconds = totalSeconds?.rem(60)
    return String.format("%d:%02d", minutes, seconds)
}