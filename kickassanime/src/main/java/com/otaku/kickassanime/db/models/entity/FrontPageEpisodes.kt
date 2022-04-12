package com.otaku.kickassanime.db.models.entity

import androidx.room.Entity
import org.threeten.bp.OffsetDateTime

@Entity(
    tableName = "front_page_episodes",
    primaryKeys = ["episodeSlugId"]
)
data class FrontPageEpisodes(
    val episodeSlugId: Int,
    val date: OffsetDateTime?
)
