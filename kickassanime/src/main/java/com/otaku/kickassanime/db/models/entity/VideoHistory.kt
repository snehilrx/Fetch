package com.otaku.kickassanime.db.models.entity

import androidx.room.Entity
import org.threeten.bp.LocalDateTime

@Entity(
    tableName = "video_history",
    primaryKeys = ["episodeSlug"],
    foreignKeys = [androidx.room.ForeignKey(
        entity = EpisodeEntity::class,
        parentColumns = ["episodeSlug"],
        childColumns = ["episodeSlug"]
    )]
)
data class VideoHistory(
    val episodeSlug: String,
    val timestamp: Long,
    val lastPlayed: LocalDateTime
)