package com.example.sora.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.material.chip.Chip

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

@Composable
fun SettingCard() {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp, 0.dp)
            .height(88.dp)
    ) {
        repeat(3) {
            SettingOptionBox(modifier = Modifier.weight(1f))
            if (it < 2) Spacer(modifier = Modifier.width(8.dp))
        }
    }

}

@Composable
fun SettingOptionBox(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .border(
                width = 1.dp,
                color = Color.Black.copy(alpha = 0.1f),
                shape = RoundedCornerShape(4.dp)
            )
            .fillMaxSize()
    )
}

@Composable
fun LanguageCard() {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp, 0.dp)
    ) {
        Text(
            text = "Language",
            fontWeight = FontWeight.W500,
            fontSize = 14.sp,
        )

        Spacer(modifier = Modifier.height(4.dp))

        Row{
            Box(
                modifier = Modifier
                    .width(64.dp)
                    .height(36.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.Black.copy(alpha = 0.05F))
            ) {
                Text(
                    text = "English",
                    fontWeight = FontWeight.W400,
                    fontSize = 14.sp,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Select your preferred language",
            fontWeight = FontWeight.W400,
            fontSize = 12.sp,
            color = Color.Black.copy(alpha = 0.5F)
        )

    }
}

@Composable
fun AccountSettings() {
    Column(
        modifier = Modifier
            .padding(12.dp, 0.dp)
    ) {
        Text(
            text = "Account Settings",
            fontWeight = FontWeight.W500,
            fontSize = 18.sp,
        )
        Row (
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(32.dp)
                    .height(32.dp)
                    .clip(CircleShape)
                    .background(color = Color.Black.copy(alpha = 0.05F))
            ) {

            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(
                modifier = Modifier
                    .padding(0.dp, 12.dp)
            ) {
                Text(
                    text = "Change Password",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.W400,
                )
                Text(
                    text = "Update your password",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.W400,
                    color = Color.Black.copy(alpha = 0.5F)
                )
            }


        }

    }
}

@Preview(showBackground = true)
@Composable
fun SettingScreenPreview() {
    SettingScreen()
}