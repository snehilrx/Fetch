package com.otaku.kickassanime.db.models.entity

import androidx.room.Entity
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime

@Entity(
    tableName = "video_history",
    primaryKeys = ["episodeSlugId"],
    foreignKeys = [androidx.room.ForeignKey(
        entity = EpisodeEntity::class,
        parentColumns = ["episodeSlugId"],
        childColumns = ["episodeSlugId"]
    )]
)
data class VideoHistory(
    val episodeSlugId: Int,
    val timestamp: Long,
    val lastPlayed: LocalDateTime
)