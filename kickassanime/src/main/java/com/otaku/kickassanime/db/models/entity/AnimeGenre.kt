package com.otaku.kickassanime.db.models.entity

import androidx.room.Entity

@Entity(
    tableName = "anime_genre",
    primaryKeys = ["animeId", "genreId"]
)
data class AnimeGenre(
    val animeId: Int,
    val genreId: Int,
)
