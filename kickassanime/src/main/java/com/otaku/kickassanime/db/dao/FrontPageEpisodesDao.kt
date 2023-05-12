package com.otaku.kickassanime.db.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.RewriteQueriesToDropUnusedColumns
import com.otaku.fetch.db.dao.BaseDao
import com.otaku.kickassanime.db.models.AnimeTile
import com.otaku.kickassanime.db.models.entity.RecentEntity
import kotlinx.coroutines.flow.Flow
import org.threeten.bp.LocalDateTime

@Dao
interface RecentDao : BaseDao<RecentEntity> {

    @Query("select a.name as title, a.animeSlug, a.rating, e.episodeSlug, a.image, e.episodeNumber, r.pageNo from recent r, episode e, anime a where r.animeSlug is a.animeSlug and r.episodeSlug is e.episodeSlug")
    fun getRecent(): PagingSource<Int, AnimeTile>

    @Query("select a.name as title, a.animeSlug, a.rating, e.episodeSlug, a.image, e.episodeNumber, r.pageNo from recent r, episode e, anime a where r.animeSlug is a.animeSlug and r.episodeSlug is e.episodeSlug and e.language is 'ja-JP'")
    fun getRecentSub(): PagingSource<Int, AnimeTile>

    @Query("select a.name as title, a.animeSlug, a.rating, e.episodeSlug, a.image, e.episodeNumber, r.pageNo from recent r, episode e, anime a where r.animeSlug is a.animeSlug and r.episodeSlug is e.episodeSlug and e.language is not 'ja-JP'")
    fun getRecentDub(): PagingSource<Int, AnimeTile>

    @Query("select a.name as title, a.animeSlug, a.rating, e.episodeSlug, a.image, e.episodeNumber, r.pageNo from recent r, episode e, anime a where r.animeSlug is a.animeSlug and r.episodeSlug is e.episodeSlug and r.pageNo is 0")
    fun getRecentPageZero(): Flow<List<AnimeTile>>

    @Query("select a.name as title, a.animeSlug, a.rating, e.episodeSlug, a.image, e.episodeNumber, r.pageNo from recent r, episode e, anime a where r.animeSlug is a.animeSlug and r.episodeSlug is e.episodeSlug and r.pageNo is 0  and e.language is 'ja-JP'")
    fun getRecentPageZeroSub(): Flow<List<AnimeTile>>

    @Query("select a.name as title, a.animeSlug, a.rating, e.episodeSlug, a.image, e.episodeNumber, r.pageNo from recent r, episode e, anime a where r.animeSlug is a.animeSlug and r.episodeSlug is e.episodeSlug and r.pageNo is 0  and e.language is not 'ja-JP'")
    fun getRecentPageZeroDub(): Flow<List<AnimeTile>>

    @Query("delete from recent where pageNo = :page")
    suspend fun removePage(page: Int)
}

@Dao
interface LastUpdateDao {
    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT MAX(createdDate) FROM episode")
    suspend fun lastUpdate(): LocalDateTime?
}