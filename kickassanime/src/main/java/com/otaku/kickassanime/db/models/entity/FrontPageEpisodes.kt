package com.otaku.kickassanime.db.models.entity

import androidx.room.Entity

@Entity(
    tableName = "front_page_episodes",
    primaryKeys = ["episodeSlugId", "animeSlugId"]
)
data class FrontPageEpisodes(
    val animeSlugId: Int,
    val episodeSlugId: Int,
    val pageNo: Int
)
