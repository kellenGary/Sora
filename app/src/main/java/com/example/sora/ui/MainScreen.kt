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
import com.example.sora.auth.AuthRepository
import com.example.sora.auth.AuthUiState
import com.example.sora.auth.IAuthViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

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

        Button(
            onClick = {
                // Get Username
                // TODO: This should be a saved username not the email
                val currentUser = AuthRepository().getCurrentUser()
                val userEmail = currentUser?.identities?.firstOrNull()?.identityData?.jsonObject?.get("email")?.jsonPrimitive?.content
                val username = userEmail?.substringBefore('@') ?: "user"



                navController.navigate("profile/$username")
            }
        ) {
             Text("Go to Profile")
        }

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
        override val uiState: StateFlow<AuthUiState> = MutableStateFlow(AuthUiState())
        override fun signOut() {}
        override fun setErrorMessage(message: String) {}
        override fun handleSpotifyAuthResult(accessToken: String, refreshToken: String, expiresIn: Long) {}
    }

    MainScreen(navController = fakeNavController, authViewModel = fakeAuthViewModel)
}