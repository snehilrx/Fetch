package com.otaku.kickassanime.page.frontpage.list.data

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import com.otaku.kickassanime.api.KickassAnimeService
import com.otaku.kickassanime.db.KickassAnimeDb
import com.otaku.kickassanime.utils.Constraints
import com.otaku.kickassanime.utils.Utils
import javax.inject.Inject

class FrontPageListRepository @Inject constructor(
    private val database: KickassAnimeDb,
    private val kickassAnimeService: KickassAnimeService
) {

    @OptIn(ExperimentalPagingApi::class)
    fun getRecentPager() = Pager(
        config = PagingConfig(
            pageSize = Constraints.NETWORK_PAGE_SIZE,
            enablePlaceholders = true
        ),
        remoteMediator = RecentMediator(
            database,
            kickassAnimeService::getFrontPageAnimeList,
            Utils::saveRecent
        ),
    ) {
        database.recentDao().getRecent()
    }

    @OptIn(ExperimentalPagingApi::class)
    fun getSubPager() = Pager(
        config = PagingConfig(
            pageSize = Constraints.NETWORK_PAGE_SIZE,
            enablePlaceholders = true,
        ),
        remoteMediator = RecentMediator(
            database,
            kickassAnimeService::getFrontPageAnimeListSub,
            Utils::saveRecent
        ),
    ) {
        database.recentDao().getRecentSub()
    }

    @OptIn(ExperimentalPagingApi::class)
    fun getDubPager() = Pager(
        config = PagingConfig(
            pageSize = Constraints.NETWORK_PAGE_SIZE,
            enablePlaceholders = true
        ),
        remoteMediator = RecentMediator(
            database,
            kickassAnimeService::getFrontPageAnimeListDub,
            Utils::saveRecent
        ),
    ) {
        database.recentDao().getRecentDub()
    }


}