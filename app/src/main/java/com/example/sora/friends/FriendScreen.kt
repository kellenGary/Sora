package com.example.sora.friends

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

// Mutual friend data class
data class MutualFriend(val name: String, val profileImageUrl: String)

// Dummy user data class
data class User(
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

@Preview(showBackground = true)
@Composable
fun FriendScreenPreview() {
    Surface {
        FriendScreen()
    }
}
