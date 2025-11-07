package com.example.sora.friends

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage


// Mutual friend data class
private data class MutualFriend(val name: String, val profileImageUrl: String)

// Dummy user data class
private data class User(
    val id: Int,
    val name: String,
    val handle: String,
    val bio: String,
    val profileImageUrl: String,
    val mutualFriends: List<MutualFriend> = emptyList(),
    val isFollowed: Boolean = false
)

@Composable
fun FriendScreen(navController: NavController? = null) {
    var searchQuery by remember { mutableStateOf("") }
    var recommendedUsers by remember {
        mutableStateOf(
            listOf(
                User(
                    1, "Alice Johnson", "@alice", "Coffee lover. Cat person.", "https://randomuser.me/api/portraits/women/1.jpg",
                    mutualFriends = listOf(
                        MutualFriend("Bob", "https://randomuser.me/api/portraits/men/2.jpg"),
                        MutualFriend("Diana", "https://randomuser.me/api/portraits/women/4.jpg")
                    )
                ),
                User(
                    2, "Bob Smith", "@bob", "Runner. Bookworm.", "https://randomuser.me/api/portraits/men/2.jpg",
                    mutualFriends = listOf(
                        MutualFriend("Alice", "https://randomuser.me/api/portraits/women/1.jpg"),
                        MutualFriend("Charlie", "https://randomuser.me/api/portraits/men/3.jpg"),
                        MutualFriend("Eve", "https://randomuser.me/api/portraits/women/5.jpg")
                    )
                ),
                User(
                    3, "Charlie Lee", "@charlie", "Gamer. Musician.", "https://randomuser.me/api/portraits/men/3.jpg",
                    mutualFriends = listOf(
                        MutualFriend("Bob", "https://randomuser.me/api/portraits/men/2.jpg")
                    )
                ),
                User(
                    4, "Diana Prince", "@diana", "Traveler. Foodie.", "https://randomuser.me/api/portraits/women/4.jpg",
                    mutualFriends = listOf(
                        MutualFriend("Eve", "https://randomuser.me/api/portraits/women/5.jpg"),
                        MutualFriend("Alice", "https://randomuser.me/api/portraits/women/1.jpg")
                    )
                ),
                User(
                    5, "Eve Adams", "@eve", "Designer. Dreamer.", "https://randomuser.me/api/portraits/women/5.jpg",
                    mutualFriends = listOf(
                        MutualFriend("Diana", "https://randomuser.me/api/portraits/women/4.jpg")
                    )
                )
            )
        )
    }

    // Filter users based on search query
    val filteredUsers = if (searchQuery.isBlank()) recommendedUsers else recommendedUsers.filter {
        it.name.contains(searchQuery, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp)
    ) {
        Text(
            text = "Friends",
            fontWeight = FontWeight.W500,
            fontSize = 16.sp,
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search users") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )
        Text(
            text = "Recommended",
            fontWeight = FontWeight.W400,
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            filteredUsers.forEach { user ->
                UserRow(user = user, onFollowClick = {
                    recommendedUsers = recommendedUsers.map {
                        if (it.id == user.id) it.copy(isFollowed = !it.isFollowed) else it
                    }
                })
            }
        }
    }
}

@Composable
private fun UserRow(user: User, onFollowClick: () -> Unit) {
    Row(
        modifier = Modifier
            .padding(12.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AsyncImage(
            model = user.profileImageUrl,
            contentDescription = "Profile picture of ${user.name}",
            modifier = Modifier
                .background(Color.LightGray, RoundedCornerShape(50))
                .height(48.dp)
                .width(48.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = user.name,
                fontWeight = FontWeight.W600,
                fontSize = 15.sp
            )
            Text(
                text = user.handle,
                fontWeight = FontWeight.W400,
                fontSize = 13.sp,
                color = Color.Gray
            )
            if (user.mutualFriends.isNotEmpty()) {
                MutualFriendsRow(user.mutualFriends)
            }
        }
        Button(
            onClick = onFollowClick,
            shape = RoundedCornerShape(20.dp),
            enabled = !user.isFollowed
        ) {
            Text(if (user.isFollowed) "Following" else "Follow")
        }
    }
}

@Composable
private fun MutualFriendsRow(mutualFriends: List<MutualFriend>) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
        // Show up to 3 avatars, side by side (no overlap for compatibility)
        mutualFriends.take(3).forEach { friend ->
            AsyncImage(
                model = friend.profileImageUrl,
                contentDescription = "Mutual friend: ${friend.name}",
                modifier = Modifier
                    .height(20.dp)
                    .width(20.dp)
                    .background(Color.LightGray, RoundedCornerShape(50))
                    .padding(end = 4.dp)
            )
        }
        val names = mutualFriends.map { it.name }
        val summary = when (names.size) {
            1 -> "Followed by ${names[0]}"
            2 -> "Followed by ${names[0]} and ${names[1]}"
            3 -> "Followed by ${names[0]}, ${names[1]} and 1 other"
            else -> "Followed by ${names[0]}, ${names[1]} and ${names.size - 2} others"
        }
        Text(
            text = summary,
            fontSize = 11.sp,
            color = Color.Gray,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun FriendScreenPreview() {
    Surface {
        FriendScreen()
    }
}
