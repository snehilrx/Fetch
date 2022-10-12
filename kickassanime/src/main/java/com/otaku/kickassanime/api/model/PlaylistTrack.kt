package com.otaku.kickassanime.api.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class PlaylistTrack(
    @SerializedName("name") val name: String? = null,
    @SerializedName("slug") val slug: String? = null,
    @SerializedName("dub") val dub: String? = null,
    @SerializedName("title") val title: String? = null
)