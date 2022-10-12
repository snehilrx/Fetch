package com.otaku.kickassanime.db.models

import com.otaku.fetch.data.ITileData
import com.otaku.kickassanime.Strings

data class AnimeTile(
    override val title: String,
    val animeslug: String,
    val animeSlugId: Int,
    val episodeSlug: String,
    val episodeSlugId: Int,
    val image: String,
    val sector: String?,
    val episodeNumber: String,
    val pageNo: Int
) : ITileData {
    override fun areItemsTheSame(newItem: ITileData): Boolean {
        return newItem is AnimeTile && episodeSlugId == newItem.episodeSlugId
    }

    override fun areContentsTheSame(newItem: ITileData): Boolean {
        return newItem is AnimeTile && this == newItem
    }

    override val imageUrl: String
        get() = "${Strings.KICKASSANIME_URL}/uploads/$image"
    override val tags: List<String>
        get() = listOf(sector ?: "UNKNOWN", "EP $episodeNumber")
}
