package com.example.sora.features

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.sora.R

// The data class for our song information, now lives here.
data class Song(
    val user: String,
    val timeAdded: String,
    val artist: String,
    val songName: String
)

/**
 * A composable function that displays a single song post in the feed.
 */
@Composable
fun SongCard(msg: Song) {
    Card {
        Column {
            Row(modifier = Modifier.padding(all = 8.dp)) {
                Image(
                    painter = painterResource(id = R.drawable.profile_photo),
                    contentDescription = "Contact profile picture", modifier = Modifier
                        .padding(end = 8.dp)
                        .size(40.dp)
                        .align(Alignment.CenterVertically)
                        .clip(CircleShape)
                )
                Column {
                    Text(text = msg.user)
                    Text(text = msg.timeAdded)
                }
            }
            Column {
                Text(text = msg.artist)
                Text(text = msg.songName)
            }

            Image(
                painter = painterResource(id = R.drawable.the_miseducation_of_lauryn_hill_cd_lauryn_hill),
                contentDescription = "Album Cover", modifier = Modifier
                    .padding(all = 8.dp)
                    .size(150.dp)
                    .align(Alignment.CenterHorizontally)
            )
        }
    }
}
