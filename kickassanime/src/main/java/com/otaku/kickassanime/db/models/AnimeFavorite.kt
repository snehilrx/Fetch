package com.otaku.kickassanime.db.models

import com.otaku.fetch.data.BaseItem
import com.otaku.fetch.data.ITileData
import com.otaku.kickassanime.Strings

data class AnimeFavorite(
    override val title: String,
    val animeSlug: String,
    val animeSlugId: Int,
    val image: String
) : ITileData {
    override fun areItemsTheSame(newItem: ITileData): Boolean {
        return newItem is AnimeFavorite && animeSlugId == newItem.animeSlugId
    }

    override fun areContentsTheSame(newItem: ITileData): Boolean {
        return newItem is AnimeFavorite && this == newItem
    }


    override val imageUrl: String
        get() = "${Strings.KICKASSANIME_URL}images/poster/$image"

    override val tags: List<String>
        get() = emptyList()
}
