package com.example.sora.ui.settings

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

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

@Preview(showBackground = true)
@Composable
fun SettingsCardPreview() {
    SettingCard()
}