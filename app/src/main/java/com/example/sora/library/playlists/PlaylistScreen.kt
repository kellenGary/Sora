package com.example.sora.library.playlists

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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


@RequiresApi(Build.VERSION_CODES.O)
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
    
    // Add padding for the bottom nav bar and mini player
    val bottomPadding = 160.dp
    
    Box(modifier = Modifier.fillMaxSize()) {
        when {
            uiState.isLoading -> {
                // Show loading indicator
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Loading playlist...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }
            uiState.error != null -> {
                // Show error message
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Error loading playlist",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Red
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = uiState.error ?: "Unknown error",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            }
            uiState.playlist != null -> {
                // Show playlist content
                val playlist = uiState.playlist!!
                LazyColumn(
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = bottomPadding),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                ) {
                    // --- Playlist header ---
                    item {
                        PlaylistHeader(playlist)
                    }
                    item {
                        ShareStatusButton(
                            isShared = playlistViewModel.isShared,
                            onClick = {
                                val ownerName = playlist.owner.display_name ?: "Unknown"
                                val imageUrl = playlist.images.firstOrNull()?.url ?: ""
                                playlistViewModel.sharePlaylistToFeed(
                                    playlistId = playlistId,
                                    name = playlist.name,
                                    imageUrl = imageUrl,
                                    owner = ownerName
                                )
                            }
                        )
                    }

                    // --- Songs list (Paginated for performance) ---
                    val tracks = uiState.displayedTracks
                    items(
                        count = tracks.size,
                        key = { index -> "track_item_$index" }
                    ) { index ->
                        SongRow(tracks[index].track, navController)
                        
                        // Load more when approaching end (pagination trigger)
                        if (index >= tracks.size - 5 && uiState.hasMoreTracks && !uiState.isLoadingMore) {
                            LaunchedEffect(Unit) {
                                playlistViewModel.loadMoreTracks()
                            }
                        }
                    }
                    
                    // Loading indicator for pagination
                    if (uiState.isLoadingMore) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Loading more tracks...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PlaylistHeader(playlist: PlaylistDetailsResponse) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(350.dp)
    ) {
        // 1. The Atmospheric Background
        AsyncImage(
            model = playlist.images.firstOrNull()?.url,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            placeholder = painterResource(id = R.drawable.ic_launcher_foreground),
            error = painterResource(id = R.drawable.ic_launcher_foreground),
            modifier = Modifier
                .fillMaxSize()
                .blur(radius = 50.dp)
                .alpha(0.6f)
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
                    model = playlist.images.firstOrNull()?.url,
                    contentDescription = null,
                    placeholder = painterResource(id = R.drawable.ic_launcher_foreground),
                    error = painterResource(id = R.drawable.ic_launcher_foreground),
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Title
            Text(
                text = playlist.name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Created by ${playlist.owner.display_name}",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(8.dp))

            playlist.description?.let { description ->
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }
    }
}
@Composable
fun SongRow(song: Track, navController: NavController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* Play song */ }
            .padding(vertical = 8.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Song Image
        AsyncImage(
            model = song.album.images?.firstOrNull()?.url,
            placeholder = painterResource(id = R.drawable.ic_launcher_foreground),
            error = painterResource(id = R.drawable.ic_launcher_foreground),
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(8.dp)),
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

@Composable
fun ShareStatusButton(
    isShared: Boolean,
    onClick: () -> Unit
) {
    // Animate the color change for a premium feel
    val backgroundColor by animateColorAsState(
        targetValue = if (isShared) Color.DarkGray else Color.White.copy(alpha = 0.1f),
        label = "color"
    )

    val contentColor by animateColorAsState(
        targetValue = if (isShared) Color.Green else Color.White,
        label = "text"
    )

    Button(
        onClick = onClick,
        enabled = !isShared, // Disable the button once shared
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            disabledContainerColor = Color.DarkGray,
            contentColor = contentColor,
            disabledContentColor = Color.White
        ),
        shape = CircleShape,
        modifier = Modifier.height(35.dp)
    ) {
        if (isShared) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Shared")
        } else {
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Share")
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