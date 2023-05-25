package com.otaku.kickassanime.db.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.RewriteQueriesToDropUnusedColumns
import com.otaku.fetch.db.dao.BaseDao
import com.otaku.kickassanime.db.models.EpisodeAnime
import com.otaku.kickassanime.db.models.entity.EpisodeEntity
import com.otaku.kickassanime.db.models.entity.EpisodePageEntity
import com.otaku.kickassanime.page.animepage.EpisodeTile
import kotlinx.coroutines.flow.Flow

@Dao
interface EpisodeEntityDao : BaseDao<EpisodeEntity> {
    @Query(
        "SELECT e.episodeSlug as _episodeSlug, e.animeSlug as _animeSlug, e.episodeNumber as _episodeNumber, e.link1 as _link1, e.link2 as _link2," +
                " e.link3 as _link3, e.link4 as _link4, e.title as _title, e.duration as duration, " +
                " e.createdDate as _createdDate, e.next as _next, e.prev as _prev, e.animeSlug as _animeSlug," +
                " e.language as _language, e.thumbnail as _thumbnail, e.favourite as _favourite, a.* FROM episode as e," +
                " anime as a WHERE episodeSlug like :episodeSlug and  a.animeSlug like :animeSlug; "
    )
    fun getEpisodeWithAnime(episodeSlug: String, animeSlug: String): Flow<EpisodeAnime?>

    @Query("SELECT * FROM episode WHERE episodeSlug = :slug")
    suspend fun getEpisode(slug: String): EpisodeEntity?

    @Query("SELECT ep.pageNo, e.episodeNumber,  e.thumbnail, e.episodeSlug as slug, e.title, e.duration FROM episode as e, episode_page as ep where ep.episodeSlug is e.episodeSlug and e.animeSlug is :animeSlug and e.language is :language order by e.episodeNumber desc")
    fun getEpisodes(animeSlug: String, language: String): PagingSource<Int, EpisodeTile>

    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT e.* FROM episode as e where e.episodeSlug = :episodeSlug")
    fun getAnimeIdAndEpisodeNumber(episodeSlug: String): EpisodeEntity?

}

@Dao
interface EpisodePageDao : BaseDao<EpisodePageEntity>