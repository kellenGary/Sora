package com.example.sora.friends

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sora.viewmodel.IFriendsViewModel
import com.example.sora.viewmodel.UserUi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Composable
fun FriendScreen(
    viewModel: IFriendsViewModel,
) {
    val users by viewModel.filteredUsers.collectAsState(initial = emptyList())
    val searchQuery by viewModel.searchQuery.collectAsState()

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
            onValueChange = { viewModel.updateSearchQuery(it) },
            label = { Text("Search users") },
            modifier = Modifier
                .fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Recommended",
            fontWeight = FontWeight.W400,
            fontSize = 14.sp,
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(users) { user ->
                UserRow(
                    user = user,
                    onFollowClick = {
                        if (user.isFollowed) {
                            viewModel.unfollow(user.id)
                        } else {
                            viewModel.follow(user.id)
                        }
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FriendScreenPreview() {

    val fakeVM = object : IFriendsViewModel {

        private val _query = MutableStateFlow("")
        override val searchQuery: StateFlow<String> = _query

        private val _users = MutableStateFlow(
            listOf(
                UserUi("1", "Alice Johnson", null, false),
                UserUi("2", "Bob Smith", null, true),
                UserUi("3", "Charlie Kim", null, false),
                UserUi("3", "Charlie Kim", null, false),
                UserUi("3", "Charlie Kim", null, false),
                UserUi("3", "Charlie Kim", null, false),
                UserUi("3", "Charlie Kim", null, false),
                UserUi("3", "Charlie Kim", null, false),
                UserUi("3", "Charlie Kim", null, false),
                UserUi("3", "Charlie Kim", null, false),
            )
        )
        override val filteredUsers: StateFlow<List<UserUi>> = _users

        override fun updateSearchQuery(newValue: String) {
            _query.value = newValue
        }

        override fun follow(userId: String) { /* no-op for preview */ }
        override fun unfollow(userId: String) { /* no-op for preview */ }
    }

    FriendScreen(
        viewModel = fakeVM
    )
}
