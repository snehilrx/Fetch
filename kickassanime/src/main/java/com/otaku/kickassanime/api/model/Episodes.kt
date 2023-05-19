package com.otaku.kickassanime.api.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class EpisodesWithPreview(
    @SerializedName("episode_number") var episodeNumber: Float? = null,
    @SerializedName("thumbnail") var thumbnail: Images? = null,
    @SerializedName("slug") val slug: String? = null,
    var title: String? = null,
    var duration_ms: Long? = null
)

@Keep
data class Page(
    val number: Int,
)

@Keep
data class EpisodesResponse(
    val pages: List<Page>?,
    @SerializedName("result") var result: ArrayList<EpisodesWithPreview>? = arrayListOf()
)