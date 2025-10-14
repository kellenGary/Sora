package com.example.sora.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import com.example.sora.R

@Composable
fun BottomNavBar(navController: NavController) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(
                onClick = {navController.navigate("main")},
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_home),
                    contentDescription = "Home"
                )
            }
            IconButton(
                onClick = {navController.navigate("map")},
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_map),
                    contentDescription = "Map"
                )
            }
            IconButton(
                onClick = {navController.navigate("friends")},
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_friends),
                    contentDescription = "Friends"
                )
            }
            IconButton(
                onClick = {navController.navigate("profile")},
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_profile),
                    contentDescription = "Profile"
                )
            }
            IconButton(
                onClick = {navController.navigate("settings")},
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_settings_24),
                    contentDescription = "Settings"
                )
            }
        }
    }
}
