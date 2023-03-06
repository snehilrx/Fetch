package com.otaku.kickassanime.utils

import com.otaku.kickassanime.api.model.*
import com.otaku.kickassanime.db.models.entity.AnimeEntity
import com.otaku.kickassanime.db.models.entity.EpisodeEntity
import com.otaku.kickassanime.db.models.entity.VideoHistory
import org.threeten.bp.LocalDateTime

fun AnimeResponse.asAnimeEntity(): AnimeEntity {
    return AnimeEntity(
        animeId = this.animeId,
        animeSlugId = this.slug?.substringAfterLast("-")?.toInt() ?: 0,
        animeSlug = this.slug?.removeSuffix("/anime/")?.substringBeforeLast("-"),
        description = this.description,
        name = this.name,
        image = this.poster
    )
}

fun Anime.asAnimeEntity(): AnimeEntity {
    val slug = this.slug?.substringBefore("-ep")
    return AnimeEntity(
        animeSlugId = HashUtils.sha256(slug),
        animeSlug = slug,
        name = this.title,
        image = this.poster?.hq?.name?.removeSuffix("hq") ?:
        this.poster?.sm?.name?.removeSuffix("sm")
    )
}

fun Anime.asEpisodeEntity(): EpisodeEntity {
    return EpisodeEntity(
        episodeSlug = this.slug,
        episodeSlugId = HashUtils.sha256(slug),
        name = this.episodeNumber?.toString() ?: "NULL",
        createdDate = this.lastUpdate?.let { Utils.parseDateTime(it) },
        sector = if (this.isSubbed == true && this.isDubbed == true) {
            Constraints.Sector.SUB_DUB
        } else if (this.isDubbed == true) {
            Constraints.Sector.DUB
        } else {
            Constraints.Sector.SUB
        }
    )
}

@Suppress("unused")
fun AnimeInformation.asAnimeEntity(animeEntity: AnimeEntity?): AnimeEntity {
    val anime = this
    if (animeEntity == null) {
        return AnimeEntity(
            animeId = anime.aid,
            animeSlugId = anime.slugId?.toIntOrNull() ?: 0,
            rating = anime.rating,
            broadcastDay = anime.broadcastDay,
            startdate = anime.startdate?.let { Utils.parseDate(it) },
            enTitle = anime.enTitle,
            broadcastTime = anime.broadcastTime,
            source = anime.source,
            duration = anime.duration,
            enddate = anime.enddate?.let { Utils.parseDate(it) },
            status = anime.status,
            description =anime.description,
            site =anime.site,
            infoLink = anime.infoLink,
            createddate = anime.createddate,
            malId = anime.malId?.toIntOrNull(),
            simklId = anime.simklId?.toIntOrNull(),
            type = type ?: anime.type
        )
    }
    return animeEntity.apply {
        animeId = animeId ?: anime.aid ?: anime.animeId
        rating = rating ?: anime.rating
        broadcastDay = broadcastDay ?: anime.broadcastDay
        startdate = startdate ?: anime.startdate?.let { Utils.parseDate(it) }
        enTitle = enTitle ?: anime.enTitle
        broadcastTime = broadcastTime ?: anime.broadcastTime
        source = source ?: anime.source
        duration = duration ?: anime.duration
        enddate = enddate ?: anime.enddate?.let { Utils.parseDate(it) }
        status = status ?: anime.status
        description = description ?: anime.description
        site = site ?: anime.site
        infoLink = infoLink ?: anime.infoLink
        createddate = createddate ?: anime.createddate
        malId = malId ?: anime.malId?.toIntOrNull()
        simklId = simklId ?: anime.simklId?.toIntOrNull()
        type = type ?: anime.type
    }
}

fun AnimeAndEpisodeInformation.asEpisodeEntity(e: EpisodeEntity): EpisodeEntity? {
    val response = this ?: return null
    return e.apply {
        episodeId = response.episodeNumber ?: 0
        animeId = response.animeId
        link1 = response.servers.getOrNull(0)
        link2 = response.servers.getOrNull(1)
        next = response.episodeNavigation?.next
        title = response.title
        link4 = response.servers.getOrNull(2)
        prev = response.episodeNavigation?.prev
        link3 = response.servers.getOrNull(3)
    }
}

fun AnimeAndEpisodeInformation.asAnimeEntity(anime: AnimeEntity?): AnimeEntity? {
    val newAnimeData = this
    return anime?.apply {
        animeId = newAnimeData.animeId
        name = newAnimeData.title
        enTitle = newAnimeData.title
        description = newAnimeData.description
    }
}

fun AnimeInformation.asEpisodeEntity(): List<EpisodeEntity> {
    return this.episodes.map {
        EpisodeEntity(
            episodeSlug = it.slug,
            episodeSlugId = it.slug?.substringAfterLast("-")?.toInt() ?: 0,
            name = if (it.num?.length == 1) "0${it.num}" else it.num,
            createdDate = it.createddate?.let { date -> Utils.parseDateTime(date) },
            animeId = this.aid,
        )
    }.toList()
}

fun EpisodeEntity.asVideoHistory(): VideoHistory {
    return VideoHistory(
        episodeSlugId,
        lastPlayed = LocalDateTime.now(),
        timestamp = 0
    )
}

fun AnimeSearchResponse.asAnimeEntity(): AnimeEntity {
    return AnimeEntity(image = image, name = name, animeSlug = slug, animeSlugId = slugId?.toIntOrNull() ?: 0)
}