package com.otaku.kickassanime.api.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.otaku.fetch.data.ITileData
import com.otaku.kickassanime.Strings


@Keep
data class AnimeSearchResponse(
    @SerializedName("name") val name: String? = null,
    @SerializedName("slug") val slug: String? = null,
    @SerializedName("slug_id") val slugId: String? = null,
    @SerializedName("poster") val image: String? = null
) : ITileData {


    override val imageUrl: String
        get() = "${Strings.KICKASSANIME_URL}uploads/$image"
    override val tags: List<String> = emptyList()
    override val title : String
        get() = name ?: "Unknown"

    override fun areItemsTheSame(newItem: ITileData): Boolean {
        return newItem is AnimeSearchResponse && this.slug == newItem.slug
    }

    override fun areContentsTheSame(newItem: ITileData): Boolean {
        return newItem is AnimeSearchResponse && this == newItem
    }
}