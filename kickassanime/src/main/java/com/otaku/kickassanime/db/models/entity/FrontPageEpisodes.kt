package com.otaku.kickassanime.db.models.entity

import androidx.room.Entity

@Entity(
    tableName = "recent",
    primaryKeys = ["episodeSlug", "animeSlug"]
)
data class RecentEntity(
    val animeSlug: String,
    val episodeSlug: String,
    val pageNo: Int
)

@Entity(
    tableName = "popular",
    primaryKeys = ["animeSlug"]
)
data class PopularEntity(
    val animeSlug: String,
    val pageNo: Int
)

@Entity(
    tableName = "trending",
    primaryKeys = ["animeSlug"]
)
data class TrendingEntity(
    val animeSlug: String,
    val pageNo: Int
)