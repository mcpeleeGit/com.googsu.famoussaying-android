package com.googsu.famoussaying.api

import com.googsu.famoussaying.data.Quote
import retrofit2.http.GET

interface QuoteApi {
    @GET("api/famoussaying")
    suspend fun getRandomQuote(): Quote
} 