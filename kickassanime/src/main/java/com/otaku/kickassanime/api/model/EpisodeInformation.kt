package com.otaku.kickassanime.api.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class EpisodeInformation(
    @SerializedName("name") val name: String? = null,
    @SerializedName("title") val title: String? = null,
    @SerializedName("slug") val slug: String? = null,
    @SerializedName("slug_id") val slugId: String? = null,
    @SerializedName("dub") val dub: String? = null,
    @SerializedName("link1") val link1: String? = null,
    @SerializedName("link2") val link2: String? = null,
    @SerializedName("link3") val link3: String? = null,
    @SerializedName("link4") val link4: String? = null,
    @SerializedName("anime_id") val animeId: String? = null,
    @SerializedName("sector") val sector: String? = null,
    @SerializedName("createddate") val createdDate: String? = null,
    @SerializedName("next") val next: PlaylistTrack? = null,
    @SerializedName("prev") val prev: PlaylistTrack? = null,
    @SerializedName("epid") val epId: String? = null,
    @SerializedName("rating") val rating: Int? = null,
    @SerializedName("votes") val votes: String? = null,
    @SerializedName("favorited") val favourite: Boolean? = null
)