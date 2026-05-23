package com.mistweather.tv.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import com.mistweather.tv.data.model.Channel
import com.mistweather.tv.data.api.RetrofitClient
import com.mistweather.tv.ui.theme.*

@Composable
fun ChannelListItem(
    channel: Channel,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val channelColor = runCatching {
        channel.color?.let { Color(android.graphics.Color.parseColor(it)) }
    }.getOrNull() ?: MistBlue

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isFocused) CardBg.copy(alpha = 0.95f) else Color.Transparent)
            .then(
                if (isFocused) Modifier.border(1.5.dp, FocusBorder, RoundedCornerShape(8.dp))
                else Modifier
            )
            .focusable(interactionSource = interactionSource)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Channel number
        Text(
            text = channel.number.toString(),
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = if (isFocused) MistBlue else TextSecondary,
            modifier = Modifier.width(52.dp),
        )

        // Icon
        val iconUrl = RetrofitClient.iconUrl(channel.icon)
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(channelColor.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center,
        ) {
            if (iconUrl != null) {
                AsyncImage(
                    model = iconUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                )
            } else {
                Text(
                    text = channel.title.take(2).uppercase(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                )
            }
        }

        // Title + categories
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = channel.title,
                fontSize = 15.sp,
                fontWeight = if (isFocused) FontWeight.SemiBold else FontWeight.Normal,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (!channel.categories.isNullOrEmpty()) {
                Text(
                    text = channel.categories.take(3).joinToString(" · "),
                    fontSize = 11.sp,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        // Viewership
        if (channel.viewership > 0) {
            Text(
                text = "👁 ${channel.viewership}",
                fontSize = 11.sp,
                color = TextSecondary,
            )
        }

        // Live indicator
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.width(64.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (channel.online) LiveGreen else Color(0xFF444444)),
            )
            Text(
                text = if (channel.online) "LIVE" else "OFF",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (channel.online) LiveGreen else Color(0xFF666666),
            )
        }
    }
}
