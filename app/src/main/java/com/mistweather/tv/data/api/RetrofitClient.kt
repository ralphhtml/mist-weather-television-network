package com.mistweather.tv.data.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    const val API_BASE = "https://api.mistweather.com/api/v1.5/"

    // Asset UUID → image URL
    fun iconUrl(uuid: String?) = uuid?.let { "${API_BASE}assets/$it" }

    // Fallback stream URL if not returned by the channel detail endpoint
    private const val STREAM_FALLBACK = "https://stream.mistweather.com/hls/%s/index.m3u8"
    fun fallbackStreamUrl(channelId: String) = STREAM_FALLBACK.format(channelId)

    val mistApi: MistApiService by lazy {
        Retrofit.Builder()
            .baseUrl(API_BASE)
            .client(
                OkHttpClient.Builder()
                    .addInterceptor(HttpLoggingInterceptor().apply {
                        level = HttpLoggingInterceptor.Level.BASIC
                    })
                    .build()
            )
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MistApiService::class.java)
    }
}
