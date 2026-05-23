package com.mistweather.tv.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import com.mistweather.tv.data.model.Channel
import com.mistweather.tv.ui.components.*
import com.mistweather.tv.ui.theme.LiveGreen
import com.mistweather.tv.ui.theme.TextSecondary
import com.mistweather.tv.viewmodel.FilterState
import com.mistweather.tv.viewmodel.UiState

@Composable
fun HomeContent(
    state: UiState,
    onChannelSelected: (Channel) -> Unit,
    onFilterChanged: (FilterState) -> Unit,
    streamUrlFor: (Channel) -> String,
) {
    Column(modifier = Modifier.fillMaxSize()) {

        // Stream preview row (live channels only)
        Spacer(Modifier.height(12.dp))
        StreamPreviewRow(
            liveChannels = state.liveChannels,
            onChannelSelected = onChannelSelected,
            streamUrlFor = streamUrlFor,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(12.dp))

        // Filter bar
        FilterBar(
            filterState = state.filterState,
            allCategories = state.allCategories,
            onFilterChanged = onFilterChanged,
        )

        // Divider
        Spacer(Modifier.height(4.dp))

        // Channel list — fills the rest of the screen (bottom end)
        when {
            state.isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Loading channels…", color = TextSecondary, fontSize = 16.sp)
                }
            }
            state.error != null -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "Could not load channels: ${state.error}",
                        color = TextSecondary,
                        fontSize = 14.sp,
                    )
                }
            }
            else -> {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp, vertical = 0.dp),
                ) {
                    Text(
                        "${state.filteredChannels.size} channels",
                        fontSize = 12.sp,
                        color = TextSecondary,
                    )
                    if (state.filterState.liveOnly) {
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "● LIVE ONLY",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = LiveGreen,
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 32.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    items(state.filteredChannels, key = { it.id }) { channel ->
                        ChannelListItem(
                            channel = channel,
                            onClick = { onChannelSelected(channel) },
                        )
                    }
                }
            }
        }
    }
}
