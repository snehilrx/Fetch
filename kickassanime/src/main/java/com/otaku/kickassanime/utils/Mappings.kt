package com.otaku.kickassanime.utils

import com.otaku.kickassanime.api.model.AnimeInformation
import com.otaku.kickassanime.api.model.AnimeResponse
import com.otaku.kickassanime.api.model.EpisodeInformation
import com.otaku.kickassanime.db.models.entity.AnimeEntity
import com.otaku.kickassanime.db.models.entity.EpisodeEntity
import org.threeten.bp.OffsetDateTime

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

fun AnimeInformation.asAnimeEntity(): AnimeEntity {
    return AnimeEntity(
        animeId = this.animeId?.toInt() ?: 0,
        animeSlugId = this.slug?.substringAfterLast("-")?.toInt() ?: 0,
        animeslug = this.slug?.removeSuffix("/anime/")?.substringBeforeLast("-"),
        rating = this.rating,
        broadcastDay = this.broadcastDay,
        startdate = this.startdate?.let { OffsetDateTime.parse(it) },
        enTitle = this.enTitle,
        broadcastTime = this.broadcastTime,
        source = this.source,
        duration = this.duration,
        name = this.name,
        enddate = this.enddate?.let { OffsetDateTime.parse(it) },
        image = this.image,
        status = this.status,
        description = this.description,
        site = this.site,
        infoLink = this.infoLink,
        createddate = this.createddate,
        malId = this.malId?.toIntOrNull(),
        simklId = this.simklId?.toIntOrNull(),
        type = this.type
    )
}

fun EpisodeInformation.asEpisodeEntity(): EpisodeEntity {
    return EpisodeEntity(
        episodeId = this.epId?.toIntOrNull() ?: 0,
        animeId = this.animeId,
        link1 = this.link1,
        link2 = this.link2,
        next = this.next?.slug?.substringAfterLast("-")?.toInt(),
        episodeSlugId = this.slug?.substringAfterLast("-")?.toInt() ?: 0,
        createdDate = this.createdDate?.let { OffsetDateTime.parse(it) },
        episodeSlug = this.slug?.removeSuffix("/anime/")?.substringBeforeLast("-"),
        title = this.title,
        sector = this.sector,
        votes = this.votes,
        link4 = this.link4,
        rating = this.rating,
        prev = this.next?.slug?.substringAfterLast("-")?.toInt(),
        name = this.name,
        link3 = this.link3,
        favourite = this.favourite
    )
}

//fun Episodes.asEpisodeEntity(): EpisodeEntity {
//    return EpisodeEntity(
//
//    )
//}