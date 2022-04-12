package com.otaku.kickassanime.api.model

import com.google.gson.annotations.SerializedName

data class Types (
    @SerializedName("name") val name: String? = null,
    @SerializedName("slug") val slug: String? = null
)