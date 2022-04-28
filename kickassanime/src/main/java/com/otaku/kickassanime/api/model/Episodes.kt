package com.otaku.kickassanime.api.model

import com.google.gson.annotations.SerializedName

data class Episodes (
    @SerializedName("epnum") val epnum: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("slug") val slug: String? = null,
    // \/anime\/estab-life-great-escape-836031\/episode-06-994213
    @SerializedName("createddate") val createddate: String? = null,
    @SerializedName("num") val num: String? = null
)