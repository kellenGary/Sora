package com.example.sora.friends
/*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.sora.ui.theme.MontserratFontFamily
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

@Composable
@Deprecated("Mutual friends are not supported yet")
fun MutualFriendsRow(mutualFriends: List<MutualFriend>) {
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
            fontFamily = MontserratFontFamily,
            fontSize = 11.sp,
            color = Color.Gray,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

*/