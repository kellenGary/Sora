package com.example.sora.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import coil.compose.AsyncImage
import com.example.sora.R // Make sure to import your R file

@Composable
fun ProfileScreen(
    username: String,
    pfpUrl: String?,
    uniqueSongs: Int,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(18.dp, 15.dp)
    ) {
        // Top container for profile info
        ProfileHeader(
            username = username,
            pfpUrl = pfpUrl,
            subHeadingText = "$uniqueSongs unique songs played"
        )

        // The rest of your profile content can go here
        Spacer(modifier = Modifier.weight(1f)) // Pushes content below it down
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "This is the Profile Screen")
        }
        Spacer(modifier = Modifier.weight(1f)) // Pushes content above it up
    }
}

@Composable
fun ProfileHeader(
    username: String,
    pfpUrl: String?,
    subHeadingText: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically // Aligns items vertically in the center of the Row
    ) {
        AsyncImage(
            model = pfpUrl,
            contentDescription = "Profile Picture",
            placeholder = ColorPainter(Color.Gray),
            error = ColorPainter(Color.Gray),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(Color.Gray)
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
                fontWeight = FontWeight.W700, // Equivalent to Bold (700 weight)
                fontSize = 16.sp // Set font size to 16.sp
            )

            // Spacer
            Spacer(modifier = Modifier.height(4.dp))

            // Subheading text
            Text(
                text = subHeadingText,
                fontWeight = FontWeight.W300, // Equivalent to Light (300 weight)
                fontSize = 12.sp, // Set font size to 12.sp
                color = Color.Gray
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    ProfileScreen(
        username = "Ricky Bobby",
        pfpUrl = null,
        uniqueSongs = 142
    )
}
