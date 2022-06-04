package com.otaku.kickassanime.page.frontpage

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.otaku.kickassanime.api.KickassAnimeService
import com.otaku.kickassanime.db.KickassAnimeDb
import com.otaku.kickassanime.db.models.AnimeTile
import com.otaku.kickassanime.db.models.entity.FrontPageEpisodes
import com.otaku.kickassanime.utils.asAnimeEntity
import com.otaku.kickassanime.utils.asEpisodeEntity
import org.threeten.bp.LocalDateTime
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

@OptIn(ExperimentalPagingApi::class)
class FrontPageMediator @Inject constructor(
    private val database: KickassAnimeDb,
    private val networkService: KickassAnimeService
) : RemoteMediator<Int, AnimeTile>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, AnimeTile>
    ): MediatorResult {
        return try {
            val loadKey = when (loadType) {
                LoadType.REFRESH -> 0
                LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
                LoadType.APPEND -> {
                    val lastItem = state.lastItemOrNull() ?: return@load MediatorResult.Success(
                        endOfPaginationReached = true
                    )
                    lastItem.pageNo + 1
                }
            }
            if (loadKey > 5) return MediatorResult.Success(
                endOfPaginationReached = true
            )
            val response = networkService.getFrontPageAnimeList(loadKey)

            database.withTransaction {
                val anime = response?.anime?.anime?.map { it.asAnimeEntity() }
                val episode = response?.anime?.anime?.map { it.asEpisodeEntity() }
                val fpe = anime?.mapIndexed { index, animeEntity ->
                    episode?.get(index)?.let {
                        FrontPageEpisodes(
                            animeSlugId = animeEntity.animeSlugId,
                            episodeSlugId = it.episodeSlugId,
                            pageNo = loadKey
                        )
                    }
                }?.filterNotNull()
                if (anime != null && episode != null) {
                    database.animeEntityDao().insertAll(anime)
                    database.episodeEntityDao().insertAll(episode)
                }
                if (fpe != null) {
                    database.frontPageEpisodesDao().insertAll(fpe)
                }
            }
            MediatorResult.Success(
                endOfPaginationReached = response?.anime?.anime?.isEmpty() ?: true
            )
        } catch (e: IOException) {
            MediatorResult.Error(e)
        } catch (e: HttpException) {
            MediatorResult.Error(e)
        }
    }

    override suspend fun initialize(): InitializeAction {
        val cacheTimeoutInHours = 24L
        val lastUpdate = database.frontPageEpisodesDao().lastUpdate()?.minusHours(cacheTimeoutInHours)
        return if (lastUpdate?.isAfter(LocalDateTime.now()) == true) {
            InitializeAction.SKIP_INITIAL_REFRESH
        } else {
            InitializeAction.LAUNCH_INITIAL_REFRESH
        }
    }
}