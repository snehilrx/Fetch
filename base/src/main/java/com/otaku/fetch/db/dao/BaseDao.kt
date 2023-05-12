package com.otaku.fetch.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.IGNORE
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Update

@Dao
interface BaseDao<T> {
    @Insert(onConflict = REPLACE)
    suspend fun insert(vararg obj: T)

    @Insert(onConflict = IGNORE)
    suspend fun insertIgnore(vararg obj: T)

    @Insert(onConflict = IGNORE)
    suspend fun insertAll(obj: List<T>)

    @Insert(onConflict = REPLACE)
    suspend fun insertAllReplace(obj: List<T>)

    @Insert(onConflict = IGNORE)
    suspend fun insertAll(vararg obj: T)

    @Delete
    suspend fun delete(vararg obj: T)

    @Update(onConflict = IGNORE)
    suspend fun updateAll(vararg obj: T)

    @Update(onConflict = REPLACE)
    suspend fun updateAll(obj: List<T>)
}