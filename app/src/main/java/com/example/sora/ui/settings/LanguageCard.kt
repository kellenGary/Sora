package com.example.sora.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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

@Preview(showBackground = true)
@Composable
fun LanguageCardPreview() {
    LanguageCard()
}