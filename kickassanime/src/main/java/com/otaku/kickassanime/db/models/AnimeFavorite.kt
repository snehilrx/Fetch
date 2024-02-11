package com.otaku.kickassanime.db.models

import androidx.annotation.Keep
import com.otaku.fetch.data.ITileData
import com.otaku.kickassanime.Strings

@Keep
data class AnimeFavorite(
    override val title: String,
    val animeSlug: String,
    val image: String
) : ITileData {
    override fun areItemsTheSame(newItem: ITileData): Boolean {
        return newItem is AnimeFavorite && animeSlug == newItem.animeSlug
    }

    override fun areContentsTheSame(newItem: ITileData): Boolean {
        return newItem is AnimeFavorite && this == newItem
    }


    override val imageUrl: String
        get() = "${Strings.KICKASSANIME_URL}/image/poster/$image"

    override val tags: List<String>
        get() = emptyList()
}