package com.otaku.kickassanime.db.models.entity

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(
    tableName = "search_results",
    primaryKeys = ["animeSlug", "id"]
)
data class SearchResultEntity(
    val id: Int,
    val animeSlug: String,
    val page: Int,
    val index: Int,
    @ColumnInfo(name = "last_accessed") val lastAccessed: Long
)
