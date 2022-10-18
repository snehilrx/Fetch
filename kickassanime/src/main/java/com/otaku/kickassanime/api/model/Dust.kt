package com.otaku.kickassanime.api.model

import com.google.gson.annotations.SerializedName

data class Dust(
    @SerializedName("data" ) var data : ArrayList<Data> = arrayListOf()
)

data class Data (
    @SerializedName("name"   ) var name   : String? = null,
    @SerializedName("src"    ) var src    : String? = null,
    @SerializedName("rawSrc" ) var rawSrc : String? = null
)