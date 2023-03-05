package com.otaku.kickassanime.db.dao

import androidx.room.*
import androidx.room.OnConflictStrategy.Companion.IGNORE
import androidx.room.OnConflictStrategy.Companion.REPLACE

@Dao
interface BaseDao<T> {
    @Insert(onConflict = REPLACE)
    suspend fun insert(vararg obj: T)

    @Insert(onConflict = IGNORE)
    suspend fun insertAll(obj: List<T>)

    @Delete
    suspend fun delete(vararg obj: T)

    @Update(onConflict = IGNORE)
    suspend fun updateAll(vararg obj: T)
}