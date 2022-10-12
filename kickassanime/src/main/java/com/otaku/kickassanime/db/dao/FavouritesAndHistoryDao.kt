package com.otaku.kickassanime.db.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import com.otaku.kickassanime.db.models.AnimeFavorite
import com.otaku.kickassanime.db.models.AnimeHistory
import com.otaku.kickassanime.db.models.entity.VideoHistory

@Dao
interface FavouriteDao {

    @Query("update anime set favourite = :favorite where animeSlugId = :animeSlugId")
    suspend fun setFavourite(animeSlugId: Int, favorite: Int)


    @Query("select animeslug, animeSlugId, name as title, image from anime where favourite = 1 ")
    fun getAllFavourites(): PagingSource<Int, AnimeFavorite>

    @Query("select favourite from anime where animeSlugId == :animeSlugId")
    suspend fun isFavourite(animeSlugId: Int) : Boolean
}

@Dao
interface HistoryDao : BaseDao<VideoHistory> {

    @Query("select a.animeslug, a.animeSlugId as animeSlugId, a.name as title, e.episodeSlug, e.episodeSlugId as episodeSlugId, a.image, e.sector, e.name as episodeNumber,lastPlayed from video_history v, anime a, episode e where (v.episodeSlugId = e.episodeSlugId and e.animeId = a.animeId) order by lastPlayed desc")
    fun getLatestWatchedVideos(): PagingSource<Int, AnimeHistory>

    @Query("select timestamp from video_history where episodeSlugId = :episodeSlugId")
    suspend fun getPlaytime(episodeSlugId: Int) : Long


    @Query("update video_history set timestamp = :time  where episodeSlugId = :episodeSlugId")
    suspend fun setPlaytime(episodeSlugId: Int, time: Long)

}