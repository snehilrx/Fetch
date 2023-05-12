package com.otaku.kickassanime.db.models.entity

import androidx.room.ColumnInfo
import androidx.room.Entity


@Entity(
    tableName = "anime_language",
    primaryKeys = ["language", "animeSlug"],
)
data class AnimeLanguageEntity(
    val language: String,
    @ColumnInfo(index = true)
    val animeSlug: String
)
