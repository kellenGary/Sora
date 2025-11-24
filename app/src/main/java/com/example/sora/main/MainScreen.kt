package com.example.sora.main

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.example.sora.feed.FeedViewModel
import com.example.sora.map.MiniMapScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavController,
    feedViewModel: FeedViewModel = viewModel()
) {
    DisposableEffect(Unit) {
        Log.d("Home", "onCreateView called")
        feedViewModel.startPollingActiveFriends()
        onDispose {
            Log.d("Home", "onDestroyView called")
            feedViewModel.stopPollingActiveFriends()
        }
    }

    val feedUiState by feedViewModel.uiState.collectAsState()
    
    // Use the new PullToRefreshBox available in Material3 1.3+
    // It handles the nested scroll connection and state automatically
    androidx.compose.material3.pulltorefresh.PullToRefreshBox(
        isRefreshing = feedUiState.isRefreshing,
        onRefresh = { 
            feedViewModel.refreshFeed()
            // Also refresh playback state manually when pulling down
            (feedViewModel as? androidx.lifecycle.ViewModel)?.let { 
                // We need to access PlaybackViewModel here or trigger a global refresh
                // For now, the polling reduction is enough
            }
        },
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 160.dp)
        ) {
            item {
                Header()
            }

            item {
                ListeningNow(feedUiState, navController)
            }
            // Mini Map Section
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(20.dp))
                ) {
                    MiniMapScreen(navController = navController)
                }
            }

            // Recent Activity Section
            recentActivity(
                feedUiState = feedUiState,
                feedViewModel = feedViewModel,
                navController = navController
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewMainScreen() {
    val fakeNavController = rememberNavController()

    MainScreen(navController = fakeNavController)
}