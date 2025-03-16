package com.googsu.famoussaying.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object QuoteApiClient {
    private const val BASE_URL = "http://test-tam.pe.kr/"

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val quoteApi: QuoteApi = retrofit.create(QuoteApi::class.java)
} 