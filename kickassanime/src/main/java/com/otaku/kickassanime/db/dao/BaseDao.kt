package com.otaku.kickassanime.db.dao

import androidx.room.*
import androidx.room.OnConflictStrategy.REPLACE

@Dao
interface BaseDao<T> {
    @Insert(onConflict = REPLACE)
    suspend fun insert(vararg obj: T)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(obj: List<T>)

    @Delete
    suspend fun delete(vararg obj: T)

    @Update
    suspend fun updateAll(vararg episode: T)
}