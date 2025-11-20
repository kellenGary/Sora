package com.example.sora.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.sora.feed.FeedUiState

@Composable
fun ListeningNow(feedUiState: FeedUiState, navController: NavController) {
    // Get unique users with their most recent song
    val recentByUser = feedUiState.posts
        .groupBy { it.userId }
        .mapValues { (_, posts) -> posts.maxByOrNull { it.timestamp } }
        .values
        .filterNotNull()
        .sortedByDescending { it.timestamp }
        .take(5)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Horizontal scrolling list showing most recent song per user
        if (recentByUser.isEmpty() && !feedUiState.isLoading) {
            Fallback()
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(
                    items = recentByUser,
                    key = { post -> post.userId }
                ) { post ->
                    ActiveUserCard(
                        userName = post.userName ?: "User",
                        userAvatar = post.userAvatar,
                        songTitle = post.songTitle,
                        artist = post.artist,
                        albumCover = post.albumCover,
                        onClick = {
                            navController.navigate("profile?userId=${post.userId}")
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun Fallback() {
    Box {}
}