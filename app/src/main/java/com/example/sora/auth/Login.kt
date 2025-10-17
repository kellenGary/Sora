package com.example.sora.auth

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sora.R
import androidx.navigation.NavController

@Composable
fun Login(navController: NavController, authViewModel: AuthViewModel = viewModel()) {
    // Clear old messages immediately when this screen appears
    LaunchedEffect(Unit) {
        authViewModel.clearMessages()
    }

    DisposableEffect(Unit) {
        Log.d("Login", "onCreateView called")
        onDispose {
            Log.d("Login", "onDestroyView called")
        }
    }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val uiState by authViewModel.uiState.collectAsState()

    // Navigate to main screen if login successful
    LaunchedEffect(uiState.isLoggedIn) {
        if (uiState.isLoggedIn) {
            navController.navigate("main") {
                popUpTo("login") { inclusive = true }
            }
        }

        authViewModel.clearMessages()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(8, 44, 71))
    ) {
        Image(
            painter = painterResource(id = R.drawable.sora),
            contentDescription = "Sora logo",
            modifier = Modifier
                .size(300.dp)
                .padding(top = 100.dp)
                .align(Alignment.TopCenter)
        )
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(450.dp)
                .align(Alignment.BottomCenter),
            colors = CardDefaults.cardColors(containerColor = Color(243, 241, 234)),
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Login",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.Black
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isLoading
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    enabled = !uiState.isLoading
                )

                // Show error message if any
                uiState.errorMessage?.let { errorMsg ->
                    Text(
                        text = errorMsg,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Button(
                    onClick = {
                        authViewModel.clearMessages()
                        authViewModel.signIn(email, password)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(8, 44, 71)),
                    enabled = !uiState.isLoading && email.isNotBlank() && password.isNotBlank()
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Text("Login")
                    }
                }

                Text(
                    text = "Don't have an account? Sign up",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .clickable { navController.navigate("signup") },
                    color = Color(8, 44, 71)
                )
            }
        }
    }
}
