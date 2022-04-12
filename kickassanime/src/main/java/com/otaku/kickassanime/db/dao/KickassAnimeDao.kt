package com.otaku.kickassanime.db.dao

import androidx.room.Query
import com.otaku.kickassanime.db.models.AnimeTile
import java.sql.Date

interface KickassAnimeDao {

    @Query("select e.episodeSlug, e.episodeSlugId, a.animeSlug, a.animeSlugId, a.enTitle, a.image from front_page_episodes as fpe, episode as e, anime as a where :key > fpe.date and fpe.episodeSlugId = e.episodeSlugId and e.animeId = a.animeId limit :limit")
    fun getFrontPageAnimeAll(key: Date, limit: Int): List<AnimeTile>

    @Query("select e.episodeSlug, e.episodeSlugId, a.animeSlug, a.animeSlugId, a.enTitle, a.image from front_page_episodes as fpe, episode as e, anime as a where :key > fpe.date and fpe.episodeSlugId = e.episodeSlugId and e.sector == 'SUB' and e.animeId = a.animeId limit :limit")
    fun getFrontPageAnimeSub(key: Date, limit: Int): List<AnimeTile>

    @Query("select e.episodeSlug, e.episodeSlugId, a.animeSlug, a.animeSlugId, a.enTitle, a.image from front_page_episodes as fpe, episode as e, anime as a where :key > fpe.date and fpe.episodeSlugId = e.episodeSlugId and e.sector == 'DUB' and e.animeId = a.animeId limit :limit")
    fun getFrontPageAnimeDub(key: Date, limit: Int): List<AnimeTile>

}