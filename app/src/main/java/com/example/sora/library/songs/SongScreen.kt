package com.example.sora.library.songs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
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

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ){
        item {
            AlbumRow(song)
        }
        item {
            SongRow(song)
        }
    }
}
@Composable
fun AlbumRow(song: SpotifyTrackResponse?){
    Row(modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp)
    ) {
        AsyncImage(
            model = song?.album?.images?.firstOrNull()?.url,
            contentDescription = "Album Cover",
            modifier = Modifier
                .size(150.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.Gray),
            contentScale = ContentScale.Crop

        )

        Column(modifier = Modifier
            .padding(start = 16.dp)
        ) {
            Text("Album: ${song?.album?.name ?: ""}")
            Text("Artist: ${song?.album?.artists?.firstOrNull()?.name ?: ""}")
            Text("Release Date: ${song?.album?.release_date ?: ""}")
        }
    }
}

@Composable
fun SongRow (song: SpotifyTrackResponse?) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp)
    ) {
        Text("Song: ${song?.name ?: ""}")
        Text("Duration: ${formatMillis(song?.duration_ms)}")
        Text("Track Number: ${song?.track_number ?: ""}")
        Text("Popularity: ${song?.popularity ?: ""}")
    }
}





fun formatMillis(ms: Int?): String {
    val totalSeconds = ms?.div(1000)
    val minutes = totalSeconds?.div(60)
    val seconds = totalSeconds?.rem(60)
    return String.format("%d:%02d", minutes, seconds)
}