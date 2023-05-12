package com.otaku.kickassanime.page.animepage

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import com.otaku.kickassanime.api.KickassAnimeService
import com.otaku.kickassanime.db.KickassAnimeDb
import com.otaku.kickassanime.db.dao.AnimeEntityDao
import com.otaku.kickassanime.db.dao.AnimeLanguageDao
import com.otaku.kickassanime.db.dao.FavouriteDao
import com.otaku.kickassanime.db.models.entity.AnimeEntity
import com.otaku.kickassanime.db.models.entity.AnimeLanguageEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject


class AnimeRepository @Inject constructor(
    private val kickassAnimeDb: KickassAnimeDb, private val kickassAnimeService: KickassAnimeService
) {
    private val animeDao: AnimeEntityDao = kickassAnimeDb.animeEntityDao()
    private val favouriteDao: FavouriteDao = kickassAnimeDb.favouritesDao()
    private val animeLanguageDao: AnimeLanguageDao = kickassAnimeDb.animeLanguageDao()


    @OptIn(ExperimentalPagingApi::class)
    fun getEpisodes(animeSlug: String, languageId: String) = Pager(
        config = PagingConfig(
            pageSize = 100,
            enablePlaceholders = true,
        ),
        remoteMediator = EpisodeRemoteMediator(
            animeSlug, languageId, kickassAnimeService, kickassAnimeDb
        ),
    ) {
        kickassAnimeDb.episodeEntityDao().getEpisodes(animeSlug, languageId)
    }

    fun getAnime(animeSlug: String): Flow<AnimeEntity?> {
        return animeDao.getAnimeFlowable(animeSlug)
    }

    fun getAnimeLanguage(animeSlug: String): Flow<List<AnimeLanguageEntity>> {
        return animeLanguageDao.getLanguage(animeSlug)
    }

    suspend fun setFavourite(animeSlug: String, checked: Boolean) {
        favouriteDao.setFavourite(animeSlug, if (checked) 1 else 0)
    }

    suspend fun fetchLanguage(animeSlug: String) {
        val language = kickassAnimeService.getLanguage(animeSlug)
        kickassAnimeDb.animeLanguageDao()
            .insertAll(language.result.map { AnimeLanguageEntity(it, animeSlug) })
    }
}
