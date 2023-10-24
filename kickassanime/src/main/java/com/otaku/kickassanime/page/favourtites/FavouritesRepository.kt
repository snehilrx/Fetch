package com.otaku.kickassanime.page.favourtites

import androidx.paging.Pager
import androidx.paging.PagingConfig
import com.otaku.kickassanime.db.KickassAnimeDb
import javax.inject.Inject

class FavouritesRepository @Inject constructor(private val kickassAnimeDb: KickassAnimeDb) {
    suspend fun removeFavourite(animeSlug: String) {
        kickassAnimeDb.favouritesDao().setFavourite(animeSlug, 0)
    }

    suspend fun addToFavourites(animeSlug: String) {
        kickassAnimeDb.favouritesDao().setFavourite(animeSlug, 1)
    }

    val pager = Pager(
        PagingConfig(30)
    ) {
        kickassAnimeDb.favouritesDao().getAllFavourites()
    }.flow
}
