package com.otaku.kickassanime.db.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import com.otaku.kickassanime.db.models.AnimeTile
import com.otaku.kickassanime.db.models.entity.FrontPageEpisodes
import kotlinx.coroutines.flow.Flow
import org.threeten.bp.LocalDateTime

@Dao
interface FrontPageEpisodesDao : BaseDao<FrontPageEpisodes> {

    @Query("select image, episode.name as episodeNumber, anime.name as title, fpe.pageNo, fpe.animeSlugId, fpe.episodeSlugId, animeslug, episodeSlug, anime.type,sector  from front_page_episodes as fpe join anime on anime.animeSlugId = fpe.animeSlugId  join episode on episode.episodeSlugId = fpe.episodeSlugId order by episode.createdDate desc")
    fun getFrontPageEpisodes(): PagingSource<Int, AnimeTile>

    @Query("select image, episode.name as episodeNumber, anime.name as title, fpe.pageNo, fpe.animeSlugId, fpe.episodeSlugId, animeslug, episodeSlug, anime.type,sector  from front_page_episodes as fpe join anime on anime.animeSlugId = fpe.animeSlugId  join episode on episode.episodeSlugId = fpe.episodeSlugId and episode.sector LIKE 'DUB' order by episode.createdDate desc ")
    fun getFrontPageEpisodesDub(): PagingSource<Int, AnimeTile>

    @Query("select image, episode.name as episodeNumber, anime.name as title, fpe.pageNo, fpe.animeSlugId, fpe.episodeSlugId, animeslug, episodeSlug, anime.type,sector  from front_page_episodes as fpe join anime on anime.animeSlugId = fpe.animeSlugId  join episode on episode.episodeSlugId = fpe.episodeSlugId and episode.sector LIKE 'SUB' order by episode.createdDate desc ")
    fun getFrontPageEpisodesSub(): PagingSource<Int, AnimeTile>

    @Query("select image, episode.name as episodeNumber, anime.name as title, fpe.pageNo, fpe.animeSlugId, fpe.episodeSlugId, animeslug, episodeSlug, anime.type,sector  from front_page_episodes as fpe join anime on anime.animeSlugId = fpe.animeSlugId  join episode on episode.episodeSlugId = fpe.episodeSlugId order by episode.createdDate desc limit :upto")
    fun getFrontPageEpisodesUpto(upto: Int): Flow<List<AnimeTile>>

    @Query("select image, episode.name as episodeNumber, anime.name as title, fpe.pageNo, fpe.animeSlugId, fpe.episodeSlugId, animeslug, episodeSlug, anime.type, sector  from front_page_episodes as fpe join anime on anime.animeSlugId = fpe.animeSlugId  join episode on episode.episodeSlugId = fpe.episodeSlugId and episode.sector LIKE 'DUB' order by episode.createdDate desc limit :upto")
    fun getFrontPageEpisodesDubUpto(upto: Int): Flow<List<AnimeTile>>

    @Query("select image, episode.name as episodeNumber, anime.name as title, fpe.pageNo, fpe.animeSlugId, fpe.episodeSlugId, animeslug, episodeSlug, anime.type, sector  from front_page_episodes as fpe join anime on anime.animeSlugId = fpe.animeSlugId  join episode on episode.episodeSlugId = fpe.episodeSlugId and episode.sector LIKE 'SUB' order by episode.createdDate desc limit :upto")
    fun getFrontPageEpisodesSubUpto(upto: Int): Flow<List<AnimeTile>>

    @Query("SELECT MAX(createdDate) FROM episode")
    suspend fun lastUpdate(): LocalDateTime?


}