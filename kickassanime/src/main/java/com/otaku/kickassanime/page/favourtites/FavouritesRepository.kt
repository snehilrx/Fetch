package com.otaku.kickassanime.page.favourtites

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.liveData
import com.otaku.kickassanime.db.KickassAnimeDb
import javax.inject.Inject

class FavouritesRepository @Inject constructor(private val kickassAnimeDb: KickassAnimeDb){
    suspend fun removeFavourite(animeSlugId: Int) {
        kickassAnimeDb.favouritesDao().setFavourite(animeSlugId, 0)
    }

    suspend fun addToFavourites(animeSlugId: Int) {
        kickassAnimeDb.favouritesDao().setFavourite(animeSlugId, 1)
    }

    val pager = Pager(
        PagingConfig(30)
    ){
        kickassAnimeDb.favouritesDao().getAllFavourites()
    }.flow
}
