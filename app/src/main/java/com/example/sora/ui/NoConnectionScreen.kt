package com.example.sora.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sora.R

/**
 * Fallback screen displayed when there is no network connection.
 * Can be used as an overlay or full-screen replacement.
 *
 * @param onRetry Callback triggered when the user taps the retry button.
 * @param isFullScreen If true, displays as full screen. If false, displays as a compact card.
 */
@Composable
fun NoConnectionScreen(
    onRetry: () -> Unit = {},
    isFullScreen: Boolean = true
) {
    if (isFullScreen) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            NoConnectionContent(onRetry = onRetry)
        }
    } else {
        NoConnectionContent(onRetry = onRetry)
    }
}

/**
 * The content of the no connection screen.
 */
@Composable
private fun NoConnectionContent(onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth(0.85f)
            .wrapContentHeight()
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // WiFi Icon
        Icon(
            painter = painterResource(id = R.drawable.ic_launcher_foreground),
            contentDescription = "No WiFi",
            modifier = Modifier
                .size(80.dp)
                .background(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(16.dp),
            tint = MaterialTheme.colorScheme.error
        )

        // Title
        Text(
            text = "No Connection",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        // Subtitle
        Text(
            text = "It looks like you're offline. Please check your WiFi connection and try again.",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )

        // Retry Button
        Button(
            onClick = onRetry,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = "Try Again",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

/**
 * Compact banner-style no connection indicator for use at the top of screens.
 * Useful when you want to show the screen content but indicate no connection.
 */
@Composable
fun NoConnectionBanner(
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.error)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_launcher_foreground),
            contentDescription = "No Connection",
            modifier = Modifier.size(20.dp),
            tint = Color.White
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "No Internet Connection",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
            Text(
                text = "Check your WiFi and try again",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.9f)
            )
        }

        if (onRetry != null) {
            Button(
                onClick = onRetry,
                modifier = Modifier.height(32.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White
                ),
                contentPadding = PaddingValues(horizontal = 12.dp)
            ) {
                Text(
                    text = "Retry",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
