package com.otaku.kickassanime.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.otaku.kickassanime.db.models.entity.AnimeEntity
import com.otaku.kickassanime.page.adapters.EpisodeAdapter
import kotlinx.coroutines.flow.Flow

@Dao
interface AnimeEntityDao : BaseDao<AnimeEntity> {
    @Query("SELECT * FROM anime")
    suspend fun getAll(): List<AnimeEntity>

    @Query("SELECT * FROM anime a where animeSlugId is :animeSlugId limit 1")
    fun getAnimeFlowable(animeSlugId: Int): Flow<AnimeEntity?>


    @Query("SELECT * FROM anime a where animeSlugId is :animeSlugId limit 1")
    suspend fun getAnime(animeSlugId: Int): AnimeEntity?
}