package com.googsu.famoussaying.data

import com.google.gson.annotations.SerializedName

data class Quote(
    @SerializedName("contents")
    val text: String,
    @SerializedName("name")
    val author: String,
    val id: Int? = null
) 