package com.otaku.kickassanime.db.models.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.threeten.bp.OffsetDateTime

@Entity(
    tableName = "anime"
)
data class AnimeEntity(
    @PrimaryKey
    var animeId: Int,
    var malId: Int? = null,
    var simklId: Int? = null,
    var animeSlugId: Int,
    var name: String? = null,
    var enTitle: String? = null,
    var animeslug: String? = null,
    var description: String? = null,
    var status: String? = null,
    var image: String? = null,
    var startdate: OffsetDateTime? = null,
    var enddate: OffsetDateTime? = null,
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