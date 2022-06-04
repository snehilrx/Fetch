package com.otaku.kickassanime.page.frontpage

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import com.otaku.kickassanime.db.KickassAnimeDb
import com.otaku.kickassanime.utils.Constraints
import javax.inject.Inject

@OptIn(ExperimentalPagingApi::class)
class FrontPageRepository @Inject constructor(
    private val database: KickassAnimeDb,
    private val remoteMediator: FrontPageMediator
) {

    fun getFrontPageAllPager() = Pager(
        config = PagingConfig(
            pageSize = Constraints.NETWORK_PAGE_SIZE,
            enablePlaceholders = true,
        ),
        remoteMediator = remoteMediator,
    ) {
        database.frontPageEpisodesDao().getFrontPageEpisodes()
    }


    fun getFrontPageSubPager() = Pager(
        config = PagingConfig(
            pageSize = Constraints.NETWORK_PAGE_SIZE,
            enablePlaceholders = true,
        ),
        remoteMediator = remoteMediator,
    ) {
        database.frontPageEpisodesDao().getFrontPageEpisodesSub()
    }

    fun getFrontPageDubPager() = Pager(
        config = PagingConfig(
            pageSize = Constraints.NETWORK_PAGE_SIZE,
            enablePlaceholders = true,
        ),
        remoteMediator = remoteMediator,
    ) {
        database.frontPageEpisodesDao().getFrontPageEpisodesDub()
    }

}