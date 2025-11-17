package com.example.sora.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
                .shadow(8.dp)
                .background(MaterialTheme.colorScheme.surface)
                .navigationBarsPadding()
                .padding(vertical = 8.dp, horizontal = 8.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
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

@Composable
fun NavBarItem(
    iconRes: Int,
    contentDescription: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(
                if (isSelected) {
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
                        )
                    )
                } else {
                    Brush.linearGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Transparent
                        )
                    )
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier.size(44.dp)
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = contentDescription,
                tint = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier.size(22.dp)
            )
        }
    }
}
