package com.otaku.kickassanime.db.models.entity

import androidx.room.Entity

@Entity(
    tableName = "anime_filter",
    primaryKeys = ["animeId", "filterId"]
)
data class AnimeFilter(
    val animeId: Int,
    val filterId: Int,
)