package com.otaku.kickassanime.db.models

import androidx.room.Embedded
import com.otaku.kickassanime.db.models.entity.AnimeEntity
import com.otaku.kickassanime.db.models.entity.EpisodeEntity

data class EpisodeAnime(
    @Embedded
    val second: AnimeEntity?,
    @Embedded("_")
    val first: EpisodeEntity?
)