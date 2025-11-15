package com.example.sora.friends

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.sora.viewmodel.UserUi

@Composable
fun UserRow(user: UserUi, onFollowClick: () -> Unit) {
    Row(
        modifier = Modifier
            .padding(12.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AsyncImage(
            model = user.avatarUrl,
            contentDescription = "Profile picture of ${user.displayName}",
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color.LightGray)
        )

        Column(modifier = Modifier.weight(1f)) {
            user.displayName?.let {
                Text(
                    text = it,
                    fontWeight = FontWeight.W600,
                    fontSize = 15.sp
                )
            }
            // No support of handles currently 11/14
//            Text(
//                text = user.handle,
//                fontWeight = FontWeight.W400,
//                fontSize = 13.sp,
//                color = Color.Gray
//            )
            // No support of mutual friends currently 11/14
//            if (user.mutualFriends.isNotEmpty()) {
//                MutualFriendsRow(user.mutualFriends)
//            }
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