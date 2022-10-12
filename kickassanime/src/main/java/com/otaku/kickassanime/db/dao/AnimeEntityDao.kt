package com.otaku.kickassanime.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.otaku.kickassanime.db.models.entity.AnimeEntity

@Dao
interface AnimeEntityDao : BaseDao<AnimeEntity> {
    @Query("SELECT * FROM anime")
    suspend fun getAll(): List<AnimeEntity>

    @Query("SELECT * FROM anime a where animeSlugId is :animeSlugId limit 1")
    suspend fun getAnime(animeSlugId: Int) : AnimeEntity?
}