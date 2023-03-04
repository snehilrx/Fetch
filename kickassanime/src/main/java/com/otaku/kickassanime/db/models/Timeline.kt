package com.otaku.kickassanime.db.models

import com.google.gson.annotations.SerializedName


data class Timestamps (
    @SerializedName("at" ) var at : Float? = null
)
data class Episodes (
    @SerializedName("timestamps" ) var timestamps : ArrayList<Timestamps> = arrayListOf()
)
data class FindShow (
    @SerializedName("episodes" ) var episodes : ArrayList<Episodes> = arrayListOf()
)
data class Data (
    @SerializedName("findShow" ) var findShow : FindShow? = FindShow()
)

data class Timeline (
    @SerializedName("data" ) var data : Data? = Data()
)