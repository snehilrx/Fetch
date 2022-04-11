package com.otaku.kickassanime.api.model

import com.google.gson.annotations.SerializedName

data class Episodes (
    @SerializedName("epnum"       ) var epnum       : String? = null,
    @SerializedName("name"        ) var name        : String? = null,
    @SerializedName("slug"        ) var slug        : String? = null,
    @SerializedName("createddate" ) var createddate : String? = null,
    @SerializedName("num"         ) var num         : String? = null
)