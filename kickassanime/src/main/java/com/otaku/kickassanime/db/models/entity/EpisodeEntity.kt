package com.otaku.kickassanime.db.models.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import org.threeten.bp.OffsetDateTime

@Entity(
    tableName = "episode",
    foreignKeys = [ForeignKey(
        entity = AnimeEntity::class,
        parentColumns = ["animeId"],
        childColumns = ["animeId"]
    )]

)
data class EpisodeEntity(
    val name: String? = null,
    val title: String? = null,
    val episodeSlug: String? = null,
    val episodeSlugId: Int,
    val dub: String? = null,
    val link1: String? = null,
    val link2: String? = null,
    val link3: String? = null,
    val link4: String? = null,
    @ColumnInfo(index = true)
    val animeId: String? = null,
    val sector: String? = null,
    val createdDate: OffsetDateTime? = null,
    val next: Int? = null,
    val prev: Int? = null,
    @PrimaryKey
    val episodeId: Int,
    val rating: Int? = null,
    val votes: String? = null,
    val favourite: Boolean? = null
)