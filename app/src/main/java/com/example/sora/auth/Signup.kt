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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sora.R
import androidx.navigation.NavController
import com.example.sora.features.SpotifyAuthManager

@Composable
fun Signup(navController: NavController, authViewModel: AuthViewModel = viewModel()) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val context = LocalContext.current
    val uiState by authViewModel.uiState.collectAsState()

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
                .height(550.dp)
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
                    text = "Signup",
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

                // Spotify Connection Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (uiState.isSpotifyConnected) Color(29, 185, 84) else Color.White
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (uiState.isSpotifyConnected) {
                            Text(
                                text = "✓ Spotify Connected",
                                color = Color.White,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            TextButton(
                                onClick = { authViewModel.clearSpotifyAuth() }
                            ) {
                                Text("Disconnect", color = Color.White)
                            }
                        } else {
                            Text(
                                text = "Connect your Spotify account",
                                color = Color.Black,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Button(
                                onClick = {
                                    Log.d("SpotifyAuth", "Connect Spotify button clicked")
                                    val intent = SpotifyAuthManager.getAuthorizationRequestIntent(context)
                                    context.startActivity(intent)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(29, 185, 84)),
                                modifier = Modifier.padding(top = 8.dp)
                            ) {
                                Text("Connect Spotify", color = Color.White)
                            }
                        }
                    }
                }

                // Show error or success messages
                uiState.errorMessage?.let { errorMsg ->
                    Text(
                        text = errorMsg,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                uiState.successMessage?.let { successMsg ->
                    Text(
                        text = successMsg,
                        color = Color(91, 180, 110),
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Button(
                    onClick = {
                        authViewModel.clearMessages()
                        authViewModel.signUp(email, password)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(91, 180, 110)),
                    enabled = !uiState.isLoading && email.isNotBlank() && password.isNotBlank()
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Text("Sign Up", color = Color.Black)
                    }
                }

                Text(
                    text = "Already have an account? Login",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .clickable { navController.navigate("login") },
                    color = Color(8, 44, 71)
                )
            }
        }
    }
}
