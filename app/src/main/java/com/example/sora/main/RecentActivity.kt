package com.example.sora.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.sora.feed.FeedUiState
import com.example.sora.feed.FeedViewModel

fun LazyListScope.recentActivity(
    feedUiState: FeedUiState,
    feedViewModel: FeedViewModel,
    navController: NavController
) {

    when {
        feedUiState.isLoading && feedUiState.posts.isEmpty() -> {
            item {
                Loading()
            }
        }
        feedUiState.errorMessage != null && feedUiState.posts.isEmpty() -> {
            item {
                Error(feedUiState, feedViewModel)
            }
        }
        feedUiState.posts.isEmpty() -> {
            item {
                Empty()
            }
        }
    }

    // Section header
    item {
        Text(
            text = "Recent Activity",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
        )
    }

    // Render all feed posts
    items(
        items = feedUiState.posts,
        key = { post -> post.id }
    ) { post ->
        Box(modifier = Modifier.padding(vertical = 8.dp)) {

            val activityText = when (post.activityType.uppercase()) {
                "LIKE" -> "liked this song"
                "LISTEN" -> "recently listened"
                else -> "did something"
            }

            if (activityText == "recently listened") {
                RecentlyListenedPost(
                    post = post,
                    onClick = {
                        navController.navigate("profile?userId=${post.userId}")
                    }
                )
            } else {
                LikedSongPost(
                    post = post,
                    onClick = {
                        navController.navigate("profile?userId=${post.userId}")
                    }
                )
            }
        }
    }
}

@Composable
fun Loading() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
fun Error(feedUiState: FeedUiState, feedViewModel: FeedViewModel) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            feedUiState.errorMessage?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error
                )
            }
            IconButton(onClick = { feedViewModel.loadFeed() }) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Retry",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun Empty() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "No recent activity from friends",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}