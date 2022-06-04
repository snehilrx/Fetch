package com.otaku.kickassanime.db.models.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import org.threeten.bp.LocalDateTime

@Entity(
    tableName = "anime",
    indices = [Index(value = ["animeId"], unique = true)]
)
data class AnimeEntity(
    var animeId: Int? = null,
    var malId: Int? = null,
    var simklId: Int? = null,
    @PrimaryKey
    var animeSlugId: Int,
    var name: String? = null,
    var enTitle: String? = null,
    var animeslug: String? = null,
    var description: String? = null,
    var status: String? = null,
    var image: String? = null,
    var startdate: LocalDateTime? = null,
    var enddate: LocalDateTime? = null,
    var broadcastDay: String? = null,
    var broadcastTime: String? = null,
    var source: String? = null,
    var duration: String? = null,
    var site: String? = null,
    var infoLink: String? = null,
    var createddate: String? = null,
    var type: String? = null,
    var rating: Boolean? = null
)