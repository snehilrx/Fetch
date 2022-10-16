package com.otaku.kickassanime.api.model

import com.google.gson.annotations.SerializedName

class Beta {
    @SerializedName("data") var links: ArrayList<BetaLinks> = arrayListOf()
}

data class BetaLinks (
    @SerializedName("label" ) var label : String? = null,
    @SerializedName("file"  ) var file  : String? = null,
    @SerializedName("type"  ) var type  : String? = null
)