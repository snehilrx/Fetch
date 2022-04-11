package com.otaku.kickassanime.api.model

import com.google.gson.annotations.SerializedName

data class Genres (
    @SerializedName("name" ) var name : String? = null,
    @SerializedName("slug" ) var slug : String? = null
)