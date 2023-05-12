package com.otaku.kickassanime.api.model

data class SearchRequest(
    val query: String,
    val page: Int? = null,
    // base64 json string
    val filters: String? = null
)