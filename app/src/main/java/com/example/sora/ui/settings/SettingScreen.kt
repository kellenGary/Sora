package com.example.sora.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SettingScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp)
    ) {
        Text(
            text = "Settings",
            fontWeight = FontWeight.W500,
            fontSize = 16.sp,
        )

        Spacer(modifier = Modifier.height(18.dp))

        SettingCard()

        Spacer(modifier = Modifier.height(30.dp))

        LanguageCard()

        Spacer(modifier = Modifier.height(30.dp))

        AccountSettings()
    }
}

@Preview(showBackground = true)
@Composable
fun SettingScreenPreview() {
    SettingScreen()
}