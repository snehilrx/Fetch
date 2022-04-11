package com.otaku.kickassanime.api.model

import com.google.gson.annotations.SerializedName

data class AnimeSearchResponse(
    @SerializedName("name") val name: String? = null,
    @SerializedName("slug") val path: String? = null,
    @SerializedName("image") val image: String? = null
)