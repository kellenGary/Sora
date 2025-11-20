package com.example.sora.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.sora.R

@Composable
fun BottomNavBar(
    navController: NavController,
    isVisible: Boolean = true
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    if (isVisible) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 28.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            // Blurred background layer
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White.copy(alpha = 0.02f))
                    .blur(radius = 40.dp)
            )
            
            // Glassy foreground
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .clickable(
                        onClick = { },
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    )
                    .shadow(
                        elevation = 24.dp,
                        shape = RoundedCornerShape(20.dp),
                        spotColor = Color.Black.copy(alpha = 0.4f)
                    )
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF7A7A7A).copy(alpha = 0.95f),
                                Color(0xFF5E5E5E).copy(alpha = 0.92f),
                                Color(0xFF4E4E4E).copy(alpha = 0.90f)
                            )
                        )
                    )
                    .border(
                        width = 1.5.dp,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFFAAAAAA).copy(alpha = 0.5f),
                                Color(0xFF888888).copy(alpha = 0.3f)
                            )
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    NavBarItem(
                        iconRes = R.drawable.ic_home,
                        contentDescription = "Home",
                        isSelected = currentRoute == "main",
                        onClick = { navController.navigate("main") }
                    )
                    NavBarItem(
                        iconRes = R.drawable.ic_map,
                        contentDescription = "Map",
                        isSelected = currentRoute == "map",
                        onClick = { navController.navigate("map") }
                    )
                    NavBarItem(
                        iconRes = R.drawable.ic_album,
                        contentDescription = "Library",
                        isSelected = currentRoute == "library",
                        onClick = { navController.navigate("library") }
                    )
                    NavBarItem(
                        iconRes = R.drawable.ic_friends,
                        contentDescription = "Friends",
                        isSelected = currentRoute == "friends",
                        onClick = { navController.navigate("friends") }
                    )
                    NavBarItem(
                        iconRes = R.drawable.ic_profile,
                        contentDescription = "Profile",
                        isSelected = currentRoute?.startsWith("profile") == true,
                        onClick = { navController.navigate("profile") }
                    )
                }
            }
        }
    }
}

@Composable
fun NavBarItem(
    iconRes: Int,
    contentDescription: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(48.dp)
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = contentDescription,
            tint = if (isSelected) {
                Color.White
            } else {
                Color.White.copy(alpha = 0.6f)
            },
            modifier = Modifier
                .size(26.dp)
                .then(
                    if (isSelected) {
                        Modifier.shadow(
                            elevation = 8.dp,
                            shape = CircleShape,
                            spotColor = Color.White.copy(alpha = 0.5f)
                        )
                    } else {
                        Modifier
                    }
                )
        )
    }
}
