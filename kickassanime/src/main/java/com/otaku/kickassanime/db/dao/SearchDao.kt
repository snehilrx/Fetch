package com.otaku.kickassanime.db.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import com.otaku.fetch.db.dao.BaseDao
import com.otaku.kickassanime.db.models.entity.AnimeEntityWithPage
import com.otaku.kickassanime.db.models.entity.SearchResultEntity

@Dao
interface SearchResultEntityDao : BaseDao<SearchResultEntity> {

    @Query("SELECT * FROM search_results ORDER BY last_accessed ASC LIMIT 1")
    suspend fun getLeastRecentlyUsed(): SearchResultEntity

    @Query("DELETE FROM search_results where id = :id")
    fun deleteAllById(id: Int)

    @Query("SELECT COUNT(DISTINCT ID) from search_results")
    fun uniqueCount(): Int

    @Query("SELECT a.*, s.page as pageNumber from search_results s, anime a where s.id = :id and s.animeSlug = a.animeSlug order by s.page asc, s.`index` asc")
    fun find(id: Int): PagingSource<Int, AnimeEntityWithPage>

}