package com.mistweather.tv.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.BlendMode
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import com.mistweather.tv.R
import com.mistweather.tv.ui.theme.*
import com.mistweather.tv.viewmodel.Tab
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.Color

@Composable
fun TopBar(
    selectedTab: Tab,
    onTabSelected: (Tab) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(68.dp)
            .background(NavBg)
            .padding(horizontal = 32.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Nav tabs — left side
        NavTab(
            label = "Home",
            selected = selectedTab == Tab.HOME,
            onClick = { onTabSelected(Tab.HOME) },
        )
        Spacer(Modifier.width(8.dp))
        NavTab(
            label = "Radar / Forecast",
            selected = selectedTab == Tab.RADAR,
            onClick = { onTabSelected(Tab.RADAR) },
        )

        Spacer(Modifier.weight(1f))

        // Logo + wordmark — right side
        Image(
            painter = painterResource(R.drawable.logo_mist),
            contentDescription = "Mist",
            modifier = Modifier
                .height(44.dp)
                .wrapContentWidth()
                .graphicsLayer { blendMode = BlendMode.Screen },
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = "Television Network",
            fontSize = 18.sp,
            fontWeight = FontWeight.Light,
            letterSpacing = 1.5.sp,
            color = TextPrimary,
            modifier = Modifier.padding(end = 4.dp),
        )
    }
}

@Composable
private fun NavTab(label: String, selected: Boolean, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val highlight = selected || isFocused
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (highlight) MistBlue.copy(alpha = 0.25f) else Color.Transparent)
            .then(
                if (isFocused) Modifier.border(2.dp, FocusBorder, RoundedCornerShape(8.dp))
                else Modifier
            )
            .focusable(interactionSource = interactionSource)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 10.dp),
    ) {
        Text(
            text = label,
            fontSize = 16.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) MistBlue else TextPrimary,
            textAlign = TextAlign.Center,
        )
    }
}
