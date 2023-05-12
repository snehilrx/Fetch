package com.otaku.kickassanime.db.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import com.otaku.fetch.db.dao.BaseDao
import com.otaku.kickassanime.db.models.AnimeFavorite
import com.otaku.kickassanime.db.models.AnimeHistory
import com.otaku.kickassanime.db.models.entity.VideoHistory

@Dao
interface FavouriteDao {
    @Query("update anime set favourite = :favorite where animeSlug = :animeSlug")
    suspend fun setFavourite(animeSlug: String, favorite: Int)


    @Query("select animeSlug, name as title, image from anime where favourite = 1 ")
    fun getAllFavourites(): PagingSource<Int, AnimeFavorite>
}

@Dao
interface HistoryDao : BaseDao<VideoHistory> {

    @Query("select a.animeSlug, a.name as title, e.episodeSlug, e.episodeSlug, a.image, e.episodeNumber, v.lastPlayed from video_history v, anime a, episode e where (v.episodeSlug = e.episodeSlug and e.animeSlug = a.animeSlug) order by lastPlayed desc")
    fun getLatestWatchedVideos(): PagingSource<Int, AnimeHistory>

    @Query("select timestamp from video_history where episodeSlug = :episodeSlug")
    suspend fun getPlaytime(episodeSlug: String): Long


    @Query("update video_history set timestamp = :time  where episodeSlug = :episodeSlug")
    suspend fun setPlaytime(episodeSlug: String, time: Long)

}