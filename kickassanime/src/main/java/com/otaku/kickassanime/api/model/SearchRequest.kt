package com.otaku.kickassanime.api.model

import androidx.annotation.Keep

@Keep
data class SearchRequest(
    val query: String,
    val page: Int? = null,
    // base64 json string
    val filters: String? = null
)