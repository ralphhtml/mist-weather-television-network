package com.mistweather.tv.data.api

import com.mistweather.tv.data.model.Channel
import retrofit2.http.GET
import retrofit2.http.Path

interface MistApiService {
    @GET("channels")
    suspend fun getChannels(): List<Channel>

    @GET("channels/{id}")
    suspend fun getChannel(@Path("id") id: String): Channel
}
