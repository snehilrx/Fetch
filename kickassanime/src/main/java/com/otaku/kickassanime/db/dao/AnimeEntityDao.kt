package com.otaku.kickassanime.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.otaku.kickassanime.db.models.entity.AnimeEntity

@Dao
interface AnimeEntityDao : BaseDao<AnimeEntity> {
    @Query("SELECT * FROM anime")
    fun getAll(): List<AnimeEntity>
}