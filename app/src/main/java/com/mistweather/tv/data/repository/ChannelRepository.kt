package com.mistweather.tv.data.repository

import com.mistweather.tv.data.api.RetrofitClient
import com.mistweather.tv.data.model.Channel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ChannelRepository {
    private val api = RetrofitClient.mistApi

    suspend fun getChannels(): Result<List<Channel>> = withContext(Dispatchers.IO) {
        runCatching { api.getChannels().sortedBy { it.number } }
    }

    suspend fun getChannelDetail(id: String): Result<Channel> = withContext(Dispatchers.IO) {
        runCatching { api.getChannel(id) }
    }

    fun streamUrlFor(channel: Channel): String =
        channel.streamUrl
            ?: channel.hlsUrl
            ?: channel.stream
            ?: channel.url
            ?: RetrofitClient.fallbackStreamUrl(channel.id)
}
