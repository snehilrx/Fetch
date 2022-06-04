package com.otaku.kickassanime.db.dao

import androidx.room.*
import androidx.room.OnConflictStrategy.REPLACE

@Dao
interface BaseDao<T> {
    @Insert(onConflict = REPLACE)
    fun insert(vararg obj: T)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAll(obj: List<T>)

    @Delete
    fun delete(vararg obj: T)

    @Update
    fun updateAll(episode: List<T>)
}