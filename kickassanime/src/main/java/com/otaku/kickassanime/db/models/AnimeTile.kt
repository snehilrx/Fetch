package com.otaku.kickassanime.db.models

import com.otaku.fetch.data.ITileData
import com.otaku.kickassanime.Strings
import com.otaku.kickassanime.utils.Constraints

data class AnimeTile(
    override val title: String,
    val animeSlug: String?,
    val animeSlugId: Int,
    val episodeSlug: String?,
    val episodeSlugId: Int,
    val image: String,
    val sector: String?,
    val episodeNumber: String?,
    val pageNo: Int
) : ITileData {
    override fun areItemsTheSame(newItem: ITileData): Boolean {
        return newItem is AnimeTile && episodeSlugId == newItem.episodeSlugId
    }

    override fun areContentsTheSame(newItem: ITileData): Boolean {
        return newItem is AnimeTile && this == newItem
    }

    override val imageUrl: String
        get() = "${Strings.KICKASSANIME_URL}images/poster/$image"
    override val tags: List<String>
        get() = listOf(*getEpisodeTypeTag(sector), "EP $episodeNumber")

    private fun getEpisodeTypeTag(sector: String?): Array<String> {
        return if (sector == null) {
            emptyArray()
        } else {
            return when (sector) {
                Constraints.Sector.DUB -> arrayOf("DUB")
                Constraints.Sector.SUB -> arrayOf("SUB")
                Constraints.Sector.SUB_DUB -> arrayOf("SUB", "DUB")
                else -> emptyArray()
            }
        }
    }
}
