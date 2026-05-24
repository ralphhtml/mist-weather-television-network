package com.mistweather.tv.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import com.mistweather.tv.ui.theme.MistBlue
import com.mistweather.tv.ui.theme.TextSecondary

@Composable
fun RadarScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "Radar / Forecast",
                fontSize = 28.sp,
                fontWeight = FontWeight.Light,
                color = MistBlue,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Coming soon",
                fontSize = 16.sp,
                color = TextSecondary,
            )
        }
    }
}
