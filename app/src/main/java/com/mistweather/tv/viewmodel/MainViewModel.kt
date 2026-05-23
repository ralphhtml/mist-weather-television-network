package com.mistweather.tv.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mistweather.tv.data.model.Channel
import com.mistweather.tv.data.repository.ChannelRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class Tab { HOME, RADAR }

data class FilterState(
    val category: String? = null,
    val liveOnly: Boolean = false,
    val region: String? = null,
)

data class UiState(
    val isLoading: Boolean = true,
    val channels: List<Channel> = emptyList(),
    val liveChannels: List<Channel> = emptyList(),
    val filteredChannels: List<Channel> = emptyList(),
    val error: String? = null,
    val selectedTab: Tab = Tab.HOME,
    val filterState: FilterState = FilterState(),
    val selectedChannel: Channel? = null,
    val allCategories: List<String> = emptyList(),
)

class MainViewModel : ViewModel() {
    private val repository = ChannelRepository()

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init { loadChannels() }

    fun loadChannels() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            repository.getChannels().fold(
                onSuccess = { channels ->
                    val live = channels.filter { it.online }
                    val categories = channels
                        .flatMap { it.categories ?: emptyList() }
                        .distinct()
                        .sorted()
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        channels = channels,
                        liveChannels = live,
                        filteredChannels = channels,
                        allCategories = categories,
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load channels",
                    )
                }
            )
        }
    }

    fun selectTab(tab: Tab) {
        _uiState.value = _uiState.value.copy(selectedTab = tab)
    }

    fun selectChannel(channel: Channel) {
        _uiState.value = _uiState.value.copy(selectedChannel = channel)
    }

    fun dismissPlayer() {
        _uiState.value = _uiState.value.copy(selectedChannel = null)
    }

    fun setFilter(filter: FilterState) {
        val base = _uiState.value.channels
        val filtered = base.filter { ch ->
            val catOk = filter.category == null || ch.categories?.contains(filter.category) == true
            val liveOk = !filter.liveOnly || ch.online
            catOk && liveOk
        }
        _uiState.value = _uiState.value.copy(filterState = filter, filteredChannels = filtered)
    }

    fun streamUrlFor(channel: Channel) = repository.streamUrlFor(channel)
}
