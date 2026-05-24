package com.mistweather.tv.ui.components

import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.tv.foundation.lazy.list.TvLazyRow
import androidx.tv.foundation.lazy.list.items
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import com.mistweather.tv.data.model.Channel
import com.mistweather.tv.data.api.RetrofitClient
import com.mistweather.tv.ui.theme.*
import androidx.compose.foundation.clickable

@Composable
fun StreamPreviewRow(
    liveChannels: List<Channel>,
    onChannelSelected: (Channel) -> Unit,
    streamUrlFor: (Channel) -> String,
    modifier: Modifier = Modifier,
) {
    if (liveChannels.isEmpty()) return

    Column(modifier = modifier) {
        Text(
            text = "LIVE NOW",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp,
            color = LiveGreen,
            modifier = Modifier.padding(start = 32.dp, bottom = 8.dp),
        )
        TvLazyRow(
            contentPadding = PaddingValues(horizontal = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(liveChannels, key = { it.id }) { channel ->
                PreviewCard(
                    channel = channel,
                    streamUrl = streamUrlFor(channel),
                    onClick = { onChannelSelected(channel) },
                )
            }
        }
    }
}

@Composable
private fun PreviewCard(
    channel: Channel,
    streamUrl: String,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val context = LocalContext.current

    val exoPlayer = remember(streamUrl) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(streamUrl))
            volume = 0f
            playWhenReady = false
        }
    }
    LaunchedEffect(isFocused) {
        if (isFocused) {
            exoPlayer.prepare()
            exoPlayer.playWhenReady = true
        } else {
            exoPlayer.pause()
        }
    }
    DisposableEffect(Unit) {
        onDispose { exoPlayer.release() }
    }

    Box(
        modifier = Modifier
            .width(256.dp)
            .height(144.dp)
            .scale(if (isFocused) 1.06f else 1f)
            .clip(RoundedCornerShape(10.dp))
            .background(CardBg)
            .then(
                if (isFocused) Modifier.border(2.dp, FocusBorder, RoundedCornerShape(10.dp))
                else Modifier
            )
            .focusable(interactionSource = interactionSource)
            .clickable(onClick = onClick),
    ) {
        // Video surface (plays when focused)
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = false
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                    )
                }
            },
            modifier = Modifier.fillMaxSize(),
        )

        // Fallback thumbnail (channel banner or icon) shown when video not loaded
        if (!isFocused) {
            val thumbUrl = RetrofitClient.iconUrl(channel.banner)
                ?: RetrofitClient.iconUrl(channel.icon)
            if (thumbUrl != null) {
                AsyncImage(
                    model = thumbUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            channel.color?.let {
                                runCatching { Color(android.graphics.Color.parseColor(it)) }
                                    .getOrElse { MistBlue }
                            } ?: MistBlue
                        )
                )
            }
        }

        // Bottom gradient overlay
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .align(Alignment.BottomStart)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color(0xDD000000))
                    )
                )
        )

        // Live badge
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(LiveGreen)
                .padding(horizontal = 6.dp, vertical = 2.dp),
        ) {
            Text("LIVE", fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
        }

        // Channel number
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xAA000000))
                .padding(horizontal = 6.dp, vertical = 2.dp),
        ) {
            Text("CH ${channel.number}", fontSize = 9.sp, color = TextSecondary)
        }

        // Channel title at bottom
        Text(
            text = channel.title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = TextPrimary,
            maxLines = 1,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(8.dp),
        )
    }
}
