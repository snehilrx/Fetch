package com.otaku.kickassanime.api.model

import androidx.annotation.Keep

@Keep
data class Filters(
    val genre: List<String>,
    val types: List<String>,
    val years: List<String>,
)