package com.otaku.kickassanime.db.models

import android.text.format.DateUtils
import com.otaku.fetch.data.ITileData
import com.otaku.kickassanime.Strings
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneOffset

data class AnimeHistory(
    override val title: String,
    val animeSlug: String,
    val episodeSlug: String,
    val image: String,
    val episodeNumber: String?,
    val lastPlayed: LocalDateTime?
) : ITileData {
    override fun areItemsTheSame(newItem: ITileData): Boolean {
        return newItem is AnimeHistory && episodeSlug == newItem.episodeSlug
    }

    override fun areContentsTheSame(newItem: ITileData): Boolean {
        return newItem is AnimeHistory && this == newItem
    }

    override val imageUrl: String
        get() = "${Strings.KICKASSANIME_URL}image/poster/$image"

    override val tags: List<String>
        get() = listOf(
            "EP ${episodeNumber.toString()}", DateUtils.getRelativeTimeSpanString(
                System.currentTimeMillis(),
                (lastPlayed?.toEpochSecond(ZoneOffset.UTC) ?: 0) * 1000,
                DateUtils.MINUTE_IN_MILLIS
            ).toString()
        )
}
