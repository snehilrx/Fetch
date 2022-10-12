package com.otaku.kickassanime.db.models.entity

import android.text.format.DateUtils
import android.text.format.DateUtils.MINUTE_IN_MILLIS
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneOffset

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
    var title: String? = null,
    val episodeSlug: String? = null,
    @PrimaryKey
    val episodeSlugId: Int,
    val dub: String? = null,
    var link1: String? = null,
    var link2: String? = null,
    var link3: String? = null,
    var link4: String? = null,
    @ColumnInfo(index = true)
    var animeId: String? = null,
    var sector: String? = null,
    var createdDate: LocalDateTime? = null,
    var next: Int? = null,
    var prev: Int? = null,
    var episodeId: Int? = null,
    var rating: Int? = null,
    var votes: String? = null,
    var favourite: Boolean? = null
) {
    val timeAgo: String
        get() {
            val date = createdDate ?: return ""
            val relativeTimeSpanString = DateUtils.getRelativeTimeSpanString(
                date.toEpochSecond(ZoneOffset.UTC),
                System.currentTimeMillis(),
                MINUTE_IN_MILLIS
            )
            return "Uploaded : $relativeTimeSpanString"
        }
}