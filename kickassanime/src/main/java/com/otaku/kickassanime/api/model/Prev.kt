package com.otaku.kickassanime.api.model

import com.google.gson.annotations.SerializedName

data class Prev (
    @SerializedName("name"  ) var name  : String? = null,
    @SerializedName("slug"  ) var slug  : String? = null,
    @SerializedName("dub"   ) var dub   : String? = null,
    @SerializedName("title" ) var title : String? = null
)