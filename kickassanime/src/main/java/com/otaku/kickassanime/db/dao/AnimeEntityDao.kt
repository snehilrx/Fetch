package com.otaku.kickassanime.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.otaku.fetch.db.dao.BaseDao
import com.otaku.kickassanime.db.models.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AnimeEntityDao : BaseDao<AnimeEntity> {

    @Query("SELECT * FROM anime as a where a.animeSlug is :animeSlug limit 1")
    fun getAnimeFlowable(animeSlug: String): Flow<AnimeEntity?>


    @Query("SELECT * FROM anime as a where a.animeSlug = :animeSlug limit 1")
    suspend fun getAnime(animeSlug: String): AnimeEntity?

    @Query("SELECT name FROM anime where animeSlug = :animeSlug")
    suspend fun getAnimeName(animeSlug: String?): String?
}

@Dao
interface AnimeLanguageDao : BaseDao<AnimeLanguageEntity> {
    @Query("SELECT * FROM anime_language where animeSlug = :animeSlug")
    fun getLanguage(animeSlug: String): Flow<List<AnimeLanguageEntity>>
}
