package com.otaku.kickassanime.db.models

import com.otaku.fetch.data.ITileData
import com.otaku.kickassanime.Strings

data class AnimeTile(
    override val title: String?,
    val animeSlug: String,
    val episodeSlug: String?,
    val image: String,
    val rating: String?,
    val episodeNumber: Int,
    val pageNo: Int
) : ITileData {
    override fun areItemsTheSame(newItem: ITileData): Boolean {
        return newItem is AnimeTile && episodeSlug == newItem.episodeSlug
    }

    override fun areContentsTheSame(newItem: ITileData): Boolean {
        return newItem is AnimeTile && this == newItem
    }

    override val imageUrl: String
        get() = "${Strings.KICKASSANIME_URL}image/poster/$image"
    override val tags: List<String>
        get() = listOfNotNull(rating, "EP $episodeNumber")
}