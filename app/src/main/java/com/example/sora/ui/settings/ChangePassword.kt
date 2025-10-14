package com.example.sora.ui.settings

import androidx.activity.result.launch
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.sora.auth.AuthUiState
import com.example.sora.auth.AuthViewModel
import com.example.sora.auth.IAuthViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import androidx.compose.material3.TopAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(navController: NavController, authViewModel: IAuthViewModel) {
    val uiState by authViewModel.uiState.collectAsState()
    var password by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let { message ->
            // Launch a coroutine to show the snackbar
            scope.launch {
                snackbarHostState.showSnackbar(message)
            }
            delay(1500L)
            authViewModel.clearMessages()
            navController.popBackStack()
        }
    }


    LaunchedEffect(Unit) {
        if (uiState.successMessage == "Login successful!") {
            authViewModel.clearMessages()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Change Password") }, // Sets the title of the screen
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding) // Use padding from Scaffold
                .padding(16.dp)
        ) {
            Text("Change Password")
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("New Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { authViewModel.changePassword(password) },
                enabled = !uiState.isLoading
            ) {
                Text("Change Password")
            }

            uiState.errorMessage?.let {
                Text(it)
            }
        }
    }
}

class FakeAuthViewModel : IAuthViewModel {
    private val _uiState = MutableStateFlow(AuthUiState())
    override val uiState: StateFlow<AuthUiState> = _uiState

    override fun signOut() {}

    override fun setErrorMessage(message: String) {}

    override fun handleSpotifyAuthResult(accessToken: String, refreshToken: String, expiresIn: Long) {}

    override fun changePassword(password: String) { }

    override fun clearMessages() { }
}

@Preview(showBackground = true)
@Composable
fun ChangePasswordScreenPreview() {
    val fakeNavController = rememberNavController()
    ChangePasswordScreen(fakeNavController, authViewModel = FakeAuthViewModel())
}