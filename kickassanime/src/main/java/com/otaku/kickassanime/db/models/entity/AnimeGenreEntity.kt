package com.otaku.kickassanime.db.models.entity

import androidx.room.Entity

@Entity(
    tableName = "anime_genre",
    primaryKeys = ["animeSlug", "genre"]
)
data class AnimeGenreEntity(
    val animeSlug: String,
    val genre: String,
)
