package com.example.sora.library.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage

private const val TAG = "LibraryScreen"

@Composable
fun LibraryScreen(
    navController: NavController? = null,
    libraryViewModel: LibraryViewModel = viewModel()
) {
    val uiState by libraryViewModel.uiState.collectAsState()


    when {
        uiState.isLoading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        uiState.error != null -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Error: ${uiState.error}",
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
        uiState.playlists.isEmpty() -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No playlists found. Connect your Spotify account in settings.",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
        else -> {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(uiState.playlists) { playlist ->
                    PlaylistItem(playlist = playlist, modifier = Modifier.padding(4.dp)
                        .clickable(onClick = {navController?.navigate("playlist/${playlist.id}")}))
                }
            }
        }
    }

}

@Composable
fun PlaylistItem(playlist: PlaylistItem, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .aspectRatio(1f)
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AsyncImage(
                model = playlist.images.firstOrNull()?.url,
                contentDescription = "Playlist Cover Art",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Gray)
            )
            Text(
                text = playlist.name,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                maxLines = 2, // Allow for slightly longer names
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LibraryScreenPreview() {
    LibraryScreen(libraryViewModel = LibraryViewModel())
}

// Preview for a single playlist item
@Preview(showBackground = true)
@Composable
fun PlaylistItemPreview() {
    val samplePlaylist = PlaylistItem(
        id = "1",
        name = "My Playlist",
        images = listOf(PlaylistImage(url = ""))
    )
    PlaylistItem(playlist = samplePlaylist)
}