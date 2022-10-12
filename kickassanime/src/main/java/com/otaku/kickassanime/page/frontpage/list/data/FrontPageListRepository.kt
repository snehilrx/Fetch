package com.otaku.kickassanime.page.frontpage.list.data

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import com.otaku.kickassanime.api.KickassAnimeService
import com.otaku.kickassanime.db.KickassAnimeDb
import com.otaku.kickassanime.utils.Constraints
import javax.inject.Inject

class FrontPageListRepository @Inject constructor(
    private val database: KickassAnimeDb,
    private val kickassAnimeService: KickassAnimeService
) {

    @OptIn(ExperimentalPagingApi::class)
    fun getFrontPageAllPager() = Pager(
        config = PagingConfig(
            pageSize = Constraints.NETWORK_PAGE_SIZE,
            enablePlaceholders = true,
        ),
        remoteMediator = FrontPageListMediator(database, kickassAnimeService::getFrontPageAnimeList),
    ) {
        database.frontPageEpisodesDao().getFrontPageEpisodes()
    }

    @OptIn(ExperimentalPagingApi::class)
    fun getFrontPageDubPager() = Pager(
        config = PagingConfig(
            pageSize = Constraints.NETWORK_PAGE_SIZE,
            enablePlaceholders = true,
        ),
        remoteMediator = FrontPageListMediator(database, kickassAnimeService::getFrontPageAnimeListDub),
    ) {
        database.frontPageEpisodesDao().getFrontPageEpisodesDub()
    }

    @OptIn(ExperimentalPagingApi::class)
    fun getFrontPageSubPager() = Pager(
        config = PagingConfig(
            pageSize = Constraints.NETWORK_PAGE_SIZE,
            enablePlaceholders = true,
        ),
        remoteMediator = FrontPageListMediator(database, kickassAnimeService::getFrontPageAnimeListSub),
    ) {
        database.frontPageEpisodesDao().getFrontPageEpisodesSub()
    }


}