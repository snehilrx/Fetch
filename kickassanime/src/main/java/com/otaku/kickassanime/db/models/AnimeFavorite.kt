package com.otaku.kickassanime.db.models

import android.icu.text.DateFormat
import android.text.format.DateUtils
import com.otaku.fetch.data.ITileData
import com.otaku.kickassanime.Strings
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneOffset

data class AnimeFavorite(
    override val title: String,
    val animeslug: String,
    val animeSlugId: Int,
    val image: String
) : ITileData {
    override fun areItemsTheSame(newItem: ITileData): Boolean {
        return newItem is AnimeFavorite && animeSlugId == newItem.animeSlugId
    }

    override fun areContentsTheSame(newItem: ITileData): Boolean {
        return newItem is AnimeFavorite && this == newItem
    }

    override val imageUrl: String
        get() = "${Strings.KICKASSANIME_URL}/uploads/$image"

    override val tags: List<String>
        get() = emptyList()
}
