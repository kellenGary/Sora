package com.example.sora.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.sora.auth.AuthViewModel
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.example.sora.auth.IAuthViewModel

@Composable
fun MainScreen(
    navController: NavController,
    authViewModel: IAuthViewModel = viewModel<AuthViewModel>()
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Welcome to Sora!",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                authViewModel.signOut()
                navController.navigate("login") {
                    popUpTo(0) { inclusive = true }
                }
            }
        ) {
            Text("Sign Out")
        }
    }
}


@Preview(showBackground = true)
@Composable
fun PreviewMainScreen() {
    val fakeNavController = rememberNavController()

    // Create a fake ViewModel for preview purposes
    val fakeAuthViewModel = object : IAuthViewModel {
        override fun signOut() {
        }
    }

    MainScreen(navController = fakeNavController, authViewModel = fakeAuthViewModel)
}