package com.otaku.kickassanime.utils

import com.otaku.kickassanime.api.model.*
import com.otaku.kickassanime.db.models.entity.AnimeEntity
import com.otaku.kickassanime.db.models.entity.EpisodeEntity
import com.otaku.kickassanime.db.models.entity.VideoHistory
import org.threeten.bp.LocalDateTime

fun AnimeResponse.asAnimeEntity(): AnimeEntity {
    return AnimeEntity(
        animeId = this.animeId?.toInt() ?: 0,
        animeSlugId = this.slug?.substringAfterLast("-")?.toInt() ?: 0,
        animeslug = this.slug?.removeSuffix("/anime/")?.substringBeforeLast("-"),
        description = this.description,
        name = this.name,
        image = this.poster
    )
}

fun Anime.asAnimeEntity(): AnimeEntity {
    return AnimeEntity(
        animeSlugId = this.slug?.substringBeforeLast("/")?.substringAfterLast("-")?.toInt() ?: 0,
        animeslug = this.slug?.removeSuffix("/anime/")?.substringBeforeLast("/"),
        name = this.name,
        image = this.poster
    )
}

fun Anime.asEpisodeEntity(): EpisodeEntity {
    return EpisodeEntity(
        episodeSlug = this.slug,
        episodeSlugId = this.slug?.substringAfterLast("-")?.toInt() ?: 0,
        name = this.episode,
        createdDate = this.episodeDate?.let { Utils.parseDateTime(it) },
        sector = this.type
    )
}

@Suppress("unused")
fun AnimeInformation.asAnimeEntity(animeEntity: AnimeEntity?): AnimeEntity {
    val anime = this
    if (animeEntity == null) {
        return AnimeEntity(
            animeId = anime.aid?.toIntOrNull() ?: 0,
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
        animeId = animeId ?: anime.aid?.toInt() ?: anime.animeId?.toInt() ?: 0
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

fun EpisodeInformation.asEpisodeEntity(e: EpisodeEntity): EpisodeEntity {
    val response = this
    return e.apply {
        episodeId = response.epId?.toIntOrNull() ?: 0
        animeId = response.animeId
        link1 = response.link1
        link2 = response.link2
        next = response.next?.slug?.substringAfterLast("-")?.toInt()
        title = response.title
        votes = response.votes
        link4 = response.link4
        rating = response.rating
        prev = response.prev?.slug?.substringAfterLast("-")?.toInt()
        link3 = response.link3
        favourite = response.favourite
    }
}

fun AnimeAndEpisodeInformation.asEpisodeEntity(e: EpisodeEntity): EpisodeEntity? {
    val response = this.episodeInformation ?: return null
    return response.asEpisodeEntity(e)
}

fun AnimeAndEpisodeInformation.asEpisodeEntity(): List<EpisodeEntity> {
    return this.episodes.map {
        EpisodeEntity(
            episodeSlug = it.slug,
            episodeSlugId = it.slug?.substringAfterLast("-")?.toInt() ?: 0,
            name = if (it.num?.length == 1) "0${it.num}" else it.num,
            createdDate = it.createddate?.let { date -> Utils.parseDateTime(date) },
            animeId = this.anime?.animeId,
        )
    }.toList()
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
    return AnimeEntity(image = image, name = name, animeslug = slug, animeSlugId = slugId?.toIntOrNull() ?: 0)
}

private object Constants {
    @JvmStatic
    val regex = Regex("Dub", RegexOption.IGNORE_CASE)
}