package com.mistweather.tv.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mistweather.tv.R
import com.mistweather.tv.ui.components.TopBar
import com.mistweather.tv.ui.theme.MistDark
import com.mistweather.tv.viewmodel.MainViewModel
import com.mistweather.tv.viewmodel.Tab

@Composable
fun MainScreen(vm: MainViewModel = viewModel()) {
    val state by vm.uiState.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize()) {
        // Background
        Image(
            painter = painterResource(R.drawable.bg_halftone),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
        // Dark overlay for readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xCC010D1F)),
        )

        // App UI
        Column(modifier = Modifier.fillMaxSize()) {
            TopBar(
                selectedTab = state.selectedTab,
                onTabSelected = vm::selectTab,
            )
            when (state.selectedTab) {
                Tab.HOME -> HomeContent(
                    state = state,
                    onChannelSelected = vm::selectChannel,
                    onFilterChanged = vm::setFilter,
                    streamUrlFor = vm::streamUrlFor,
                )
                Tab.RADAR -> RadarScreen()
            }
        }

        // Fullscreen player overlay
        state.selectedChannel?.let { channel ->
            PlayerScreen(
                channel = channel,
                streamUrl = vm.streamUrlFor(channel),
                onDismiss = vm::dismissPlayer,
            )
        }
    }
}
