package com.example.sora.friends

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

@Composable
fun UserRow(user: User, onFollowClick: () -> Unit) {
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