package com.otaku.kickassanime.utils

import com.otaku.kickassanime.api.model.EpisodeApiResponse
import com.otaku.kickassanime.api.model.EpisodesWithPreview
import com.otaku.kickassanime.api.model.Recent
import com.otaku.kickassanime.api.model.SearchItem
import com.otaku.kickassanime.db.models.entity.AnimeEntity
import com.otaku.kickassanime.db.models.entity.AnimeGenreEntity
import com.otaku.kickassanime.db.models.entity.AnimeLanguageEntity
import com.otaku.kickassanime.db.models.entity.EpisodeEntity
import com.otaku.kickassanime.db.models.entity.VideoHistory
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneOffset


fun Recent.asAnimeEntity(): AnimeEntity {
    return AnimeEntity(
        animeSlug = slug ?: "",
        name = this.title ?: "",
        image = this.poster?.hq?.removeSuffix("-hq") ?: this.poster?.sm?.removeSuffix("-sm"),
        year = year,
        description = this.synopsis
    )
}

fun Recent.asEpisodeEntity(): EpisodeEntity {
    return EpisodeEntity(
        episodeSlug = getEpisodeSlug(),
        animeSlug = slug,
        duration = this.duration,
        language = this.language,
        episodeNumber = this.episodeNumber,
        createdDate = this.createdAt?.let { Utils.parseDateTime(it) },
    )
}

fun EpisodeApiResponse.asEpisodeEntity(e: EpisodeEntity): EpisodeEntity {
    val response = this
    return e.apply {
        animeSlug = response.showSlug
        link1 = response.servers.getOrNull(0)
        link2 = response.servers.getOrNull(1)
        next = response.nextEpSlug
        title = response.title
        link4 = response.servers.getOrNull(2)
        prev = response.prevEpSlug
        link3 = response.servers.getOrNull(3)
        language = response.language
        thumbnail = response.thumbnail?.hq?.removeSuffix("-hq")
            ?: response.thumbnail?.sm?.removeSuffix("-sm")
    }
}

fun EpisodeApiResponse.asAnimeEntity(anime: AnimeEntity): AnimeEntity {
    val newAnimeData = this
    return anime.apply {
        name = newAnimeData.title
        description = newAnimeData.synopsis
        animeSlug = newAnimeData.showSlug ?: ""
        poster = newAnimeData.poster
    }
}

fun EpisodeEntity.asVideoHistory(): VideoHistory {
    return VideoHistory(
        episodeSlug,
        lastPlayed = LocalDateTime.now(ZoneOffset.UTC),
        timestamp = 0
    )
}

fun SearchItem.asAnimeEntity(): AnimeEntity {
    return AnimeEntity(
        animeSlug = this.slug ?: "",
        name = this.titleEn,
        image = this.poster?.hq?.removeSuffix("-hq") ?: this.poster?.sm?.removeSuffix("-sm"),
        year = this.year,
        description = this.synopsis,
        type = this.type,
        rating = this.rating
    )
}

fun SearchItem.asLanguageEntity(): List<AnimeLanguageEntity> {
    return this.locales.map {
        AnimeLanguageEntity(
            animeSlug = this.slug ?: "",
            language = it
        )
    }
}

fun SearchItem.asAnimeGenreEntity(): List<AnimeGenreEntity> {
    return this.genres.map {
        AnimeGenreEntity(
            animeSlug = this.slug ?: "",
            genre = it
        )
    }
}

fun EpisodesWithPreview.asEpisodeEntity(
    animeSlug: String,
    language: String,
    prev: String?,
    next: String?
): EpisodeEntity {
    return EpisodeEntity(
        episodeNumber = episodeNumber,
        title = title,
        duration = duration_ms,
        episodeSlug = slug(),
        animeSlug = animeSlug,
        thumbnail = this.thumbnail?.hq?.removeSuffix("-hq")
            ?: this.thumbnail?.sm?.removeSuffix("-sm"),
        language = language,
        prev = prev,
        next = next
    )
}

fun EpisodesWithPreview.slug(): String {
    return "ep-${episodeNumber?.toInt()}-$slug"
}