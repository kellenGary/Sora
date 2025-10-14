package com.example.sora.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun AccountSettings(navController: NavController? = null) {
    Column(
        modifier = Modifier
            .padding(12.dp, 0.dp)
    ) {
        Text(
            text = "Account Settings",
            fontWeight = FontWeight.W500,
            fontSize = 18.sp,
        )

        SettingsCard(
            text = "Change Password",
            subText = "Update your password",
            image = "\uD83D\uDD11",
            onClick = { navController?.navigate("change_password") }
        )

        SettingsCard(
            text = "Two-Factor Authentication",
            subText = "Enhance security",
            image = "\uD83D\uDCF1",
            onClick = {  }
        )

        SettingsCard(
            text = "Linked Accounts",
            subText = "Manage linked services",
            image = "\uD83D\uDD17",
            onClick = {  }
        )
    }
}

@Composable
fun SettingsCard(
    text: String,
    subText: String,
    image: String,
    onClick: () -> Unit
) {
    Row (
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .width(32.dp)
                .height(32.dp)
                .clip(CircleShape)
                .background(color = Color.Black.copy(alpha = 0.05F)),
            contentAlignment = Alignment.Center
        ) {
            Text(image)
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column(
            modifier = Modifier
                .padding(0.dp, 12.dp)
        ) {
            Text(
                text = text,
                fontSize = 14.sp,
                fontWeight = FontWeight.W400,
            )
            Text(
                text = subText,
                fontSize = 12.sp,
                fontWeight = FontWeight.W400,
                color = Color.Black.copy(alpha = 0.5F)
            )
        }
    }

    HorizontalDivider(
        thickness = 1.dp,
        color = Color.LightGray
    )
}

@Preview(showBackground = true)
@Composable
fun AccountSettingsPreview() {
    AccountSettings()
}