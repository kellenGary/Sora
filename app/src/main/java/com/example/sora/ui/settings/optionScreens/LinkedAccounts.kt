package com.example.sora.ui.settings.optionScreens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.sora.R
import com.example.sora.auth.IAuthViewModel
import com.example.sora.auth.SpotifyCredentialsManager
import com.example.sora.auth.SpotifyCredentialsManager.isSpotifyConnected
import com.example.sora.features.SpotifyAuthManager
import com.example.sora.utils.FakeAuthViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LinkedAccountScreen(navController: NavController, authViewModel: IAuthViewModel) {
    val uiState by authViewModel.uiState.collectAsState()
    val context = LocalContext.current


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Linked Accounts") },
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
            Text("Services")
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    painter = painterResource(id = R.drawable.spotify),
                    contentDescription = "Spotify logo",
                    modifier = Modifier
                        .size(32.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Spotify"
                )

                Spacer(modifier = Modifier.weight(1f))

                if (uiState.isSpotifyConnected) {
                    Text("Connected")
                } else {
                    Button(
                        onClick = {
                            Log.d("SpotifyAuth", "Connect Spotify button clicked")
                            val intent = SpotifyAuthManager.getAuthorizationRequestIntent(context)
                            context.startActivity(intent)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1DB954),
                            contentColor = Color.White
                        )
                    ) {
                        Text("Connect")
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            HorizontalDivider()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LinkedAccountScreenPreview() {
    val fakeNavController = rememberNavController()
    LinkedAccountScreen(fakeNavController, authViewModel = FakeAuthViewModel())
}