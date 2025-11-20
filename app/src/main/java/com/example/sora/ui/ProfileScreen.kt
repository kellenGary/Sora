package com.example.sora.ui

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.sora.R
import com.example.sora.viewmodel.IProfileViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.ui.text.font.FontFamily

data class SongUi(
    val id: String,
    val title: String,
    val artist: String,
    val albumArtUrl: String?,
    val isLiked: Boolean = false
)

private const val TAG = "ProfileScreen"

/**
 * Main composable screen that houses the entire profile page.
 */
@Composable
fun ProfileScreen(
    navController: NavController,
    userId: String?,
    profileViewModel: IProfileViewModel,
) {
    val uiState by profileViewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        profileViewModel.loadProfile(userId)
        Log.d(TAG, "ProfileScreen Composed for user: ${uiState.displayName}")
    }

    val context = LocalContext.current

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                Log.d(TAG, "Photo selected with URI: $uri")
                profileViewModel.updateAvatar(context, uri)
            } else {
                Log.d(TAG, "No photo selected")
            }
        }
    )

    val onPfpClickHandler = if (uiState.isPersonalProfile) {
        Modifier.clickable { photoPickerLauncher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        ) }
    } else {
        Modifier
    }




    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(18.dp, 15.dp)
            .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding())
    ) {
        // Top container for profile info
        item {
            ProfileHeader(
                username = uiState.displayName ?: "User",
                pfpUrl = uiState.avatarUrl,
                modifier = onPfpClickHandler,
                subHeadingText = "${uiState.uniqueSongs} unique songs played",
                isPersonalProfile = uiState.isPersonalProfile,
                isFollowed = uiState.isFollowed,
                handleFollowClick = {
                    if (uiState.isFollowed) profileViewModel.unfollow(userId!!)
                    else profileViewModel.follow(userId!!)
                },
                navController = navController
            )
        }

        // Listening History Section
        item {
            ExpandableSongSection(
                title = "Listening History",
                songs = uiState.listeningHistory,
                profileViewModel = profileViewModel
            )
        }

        // Likes Section
        item {
            ExpandableSongSection(
                title = "Likes",
                songs = uiState.likedSongs,
                profileViewModel = profileViewModel
            )
        }
    }
}

/**
 * Header containing the pfp, username, unique songs and followers
 */
@Composable
fun ProfileHeader(
    username: String,
    pfpUrl: Any?,
    modifier: Modifier,
    subHeadingText: String,
    isPersonalProfile: Boolean,
    isFollowed: Boolean,
    handleFollowClick: () -> Unit,
    navController: NavController
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = pfpUrl,
                contentDescription = "Profile Picture",
                placeholder = ColorPainter(Color(0xFFD9D9D9)),
                error = ColorPainter(Color(0xFFD9D9D9)),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray)
                    .then(modifier)
            )

            // Spacer between image and text
            Spacer(modifier = Modifier.width(10.dp))

            // Column for Username and Subheading
            Column(
                verticalArrangement = Arrangement.Center
            ) {
                // Username
                Text(
                    text = username,
                    fontWeight = FontWeight.W700,
                    fontSize = 16.sp
                )

                // Spacer
                Spacer(modifier = Modifier.height(4.dp))

                // Subheading text
                Text(
                    text = subHeadingText,
                    fontWeight = FontWeight.W300,
                    fontSize = 12.sp,
                    color = Color.Gray
                )

                if (!isPersonalProfile) {
                    Spacer(Modifier.height(8.dp))

                    // TODO: Check if following user or not
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.DarkGray)
                            .clickable { handleFollowClick() }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if(isFollowed) "Unfollow" else "Follow",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.W600
                        )
                    }
                }
            }
        }
        if (isPersonalProfile) {
            IconButton(
                onClick = { navController.navigate("settings") },
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_settings_24),
                    contentDescription = "Settings"
                )
            }
        }
    }
}

/**
 * A reusable composable section that displays a title and a list of songs,
 * with a "See more" / "See less" button to expand or collapse the list.
 */
@Composable
fun ExpandableSongSection(
    title: String,
    songs: List<SongUi>,
    profileViewModel: IProfileViewModel,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    // The list of songs to actually display, based on the expanded state
    val displayedSongs = if (isExpanded) songs else songs.take(2)

    Column(modifier = modifier) {
        HorizontalDivider(
            thickness = 1.dp,
            color = Color.LightGray
        )
        Spacer(modifier = Modifier.height(5.dp))

        // Section Title and "See more" button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.W500,
            )
            if (songs.size > 2) {
                Text(
                    text = if (isExpanded) "See less" else "See more",
                    color = Color.Blue,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { isExpanded = !isExpanded }
                )
            }
        }

        Spacer(modifier = Modifier.height(5.dp))

        // Column to hold the song cards
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            displayedSongs.forEach { song ->
                SongCard(
                    song = song,
                    onLikeToggle = { s, _ -> profileViewModel.toggleLike(s) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp)) // Add space after the section
    }
}


/**
 * A reusable composable representing a single song card.
 */
@Composable
fun SongCard(
    song: SongUi,
    onLikeToggle: (SongUi, Boolean) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {

    // Card container
    Box(
        modifier = modifier
            .height(120.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFE7E5DE))
    ) {
        // Row to hold the album art and song details
        Row(
            modifier = Modifier
                .padding(10.dp, 12.dp)
        ) {
            // Album Art
            AsyncImage(
                model = song.albumArtUrl,
                contentDescription = "Profile Picture",
                placeholder = ColorPainter(Color(0xFFD9D9D9)),
                error = ColorPainter(Color(0xFFD9D9D9)),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .border(
                        width = 1.dp,
                        color = Color.Black,
                        shape = RoundedCornerShape(4.dp)
                    )
            )

            Spacer(modifier = Modifier.width(10.dp))

            // Song Details
            Column {
                Text(
                    text = song.title,
                    fontWeight = FontWeight.W500,
                    fontSize = 16.sp,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = song.artist,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.W300,
                    color = Color.Black
                )

                // Heart Icon at bottom right
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentSize(Alignment.BottomEnd)
                ) {
                    IconButton(
                        onClick = { onLikeToggle(song, !song.isLiked) }
                    ) {
                        Icon(
                            imageVector = if (song.isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = if (song.isLiked) "Unlike" else "Like",
                            tint = if (song.isLiked) Color.Red else Color.Gray
                        )
                    }
                }
            }
        }

    }
}

/**
 * Preview for iterative development. To use in android studio go into "split" or "design"
 * mode (top right of the editor also, alt + shift + right).
 */
@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    val fakeHistory = listOf(
        SongUi("1", "Bohemian Rhapsody", "Queen", null),
        SongUi("2", "Stairway to Heaven", "Led Zeppelin", null),
        SongUi("3", "Hotel California", "Eagles", null),
        SongUi("4", "Smells Like Teen Spirit", "Nirvana", null)
    )

    val fakeLikes = listOf(
        SongUi("5", "Blinding Lights", "The Weeknd", null),
        SongUi("6", "As It Was", "Harry Styles", null),
        SongUi("7", "good 4 u", "Olivia Rodrigo", null),
        SongUi("8", "Levitating", "Dua Lipa", null)
    )

    // --- Mock ViewModel implementing IProfileViewModel ---
    val fakeViewModel = object : IProfileViewModel {
        override val uiState = kotlinx.coroutines.flow.MutableStateFlow(
            com.example.sora.viewmodel.ProfileUiState(
                displayName = "Ricky Bobby",
                avatarUrl = null,
                uniqueSongs = 142,
                isPersonalProfile = false,
                listeningHistory = fakeHistory,
                likedSongs = fakeLikes
            )
        )

        override fun loadProfile(userId: String?) {
            // No-op for preview
        }

        override fun updateAvatar(context: android.content.Context, uri: android.net.Uri) {
            // No-op for preview
        }

        override fun toggleLike(song: SongUi) {
            // No-op for preview
        }

        override fun follow(userId: String) {
            // No-op for preview
        }

        override fun unfollow(userId: String) {
            // No-op for preview
        }
    }

    // --- Call the screen with the fake ViewModel ---
    ProfileScreen(
        navController = rememberNavController(),
        userId = "",
        profileViewModel = fakeViewModel
    )
}
