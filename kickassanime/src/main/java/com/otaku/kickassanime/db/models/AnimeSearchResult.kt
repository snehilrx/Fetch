package com.otaku.kickassanime.db.models

import com.otaku.fetch.data.ITileData
import com.otaku.kickassanime.db.models.entity.AnimeEntity

class AnimeSearchResult(
    val animeEntity: AnimeEntity,
    override val imageUrl: String = animeEntity.getImageUrl(),
    override val tags: List<String> = listOfNotNull(
        animeEntity.year.toString(),
        animeEntity.type,
        animeEntity.rating
    ),
    override val title: String = animeEntity.name ?: ""
) : ITileData {
    override fun areItemsTheSame(newItem: ITileData): Boolean {
        return newItem is AnimeSearchResult && newItem == this
    }

    override fun areContentsTheSame(newItem: ITileData): Boolean {
        return newItem is AnimeSearchResult && this.animeEntity == newItem.animeEntity
    }

}
