package com.mistweather.tv.data.model

import com.google.gson.annotations.SerializedName

data class Channel(
    val title: String = "",
    val id: String = "",
    val number: Int = 0,
    val icon: String? = null,
    val banner: String? = null,
    val background: String? = null,
    val color: String? = null,
    val categories: List<String>? = null,
    val viewership: Int = 0,
    val online: Boolean = false,
    // Stream URL — API may return any of these on the detail endpoint
    @SerializedName("stream_url") val streamUrl: String? = null,
    @SerializedName("hls_url") val hlsUrl: String? = null,
    @SerializedName("stream") val stream: String? = null,
    @SerializedName("url") val url: String? = null,
)
