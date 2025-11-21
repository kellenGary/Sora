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

@Composable
fun MainScreen(
    navController: NavController,
    feedViewModel: FeedViewModel = viewModel()
) {
    DisposableEffect(Unit) {
        Log.d("Home", "onCreateView called")
        onDispose {
            Log.d("Home", "onDestroyView called")
        }
    }

    val feedUiState by feedViewModel.uiState.collectAsState()

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

        item {
            SharedPlaylistSection(
                feedUiState = feedUiState,
                feedViewModel = feedViewModel,
                navController = navController
            )
        }
        // Recent Activity Section
        recentActivity(
            feedUiState = feedUiState,
            feedViewModel = feedViewModel,
            navController = navController
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewMainScreen() {
    val fakeNavController = rememberNavController()

    MainScreen(navController = fakeNavController)
}