package com.otaku.kickassanime.page.frontpage.list.data

import android.util.Log
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import com.otaku.fetch.base.TAG
import com.otaku.kickassanime.api.model.Anime
import com.otaku.kickassanime.db.KickassAnimeDb
import com.otaku.kickassanime.db.models.AnimeTile
import com.otaku.kickassanime.utils.Constraints.cacheTimeoutInHours
import com.otaku.kickassanime.utils.Utils
import org.threeten.bp.LocalDateTime
import retrofit2.HttpException
import java.io.IOException

@OptIn(ExperimentalPagingApi::class)
class FrontPageListMediator(
    private val database: KickassAnimeDb,
    private val networkFetch: suspend (Int) -> List<Anime>?
) : RemoteMediator<Int, AnimeTile>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, AnimeTile>
    ): MediatorResult {
        return try {
            val endPaging = MediatorResult.Success(endOfPaginationReached = true)
            val continuePaging = MediatorResult.Success(endOfPaginationReached = false)
            val loadKey = when (loadType) {
                LoadType.REFRESH -> 1
                LoadType.PREPEND -> return endPaging
                LoadType.APPEND -> {
                    val lastItem = state.lastItemOrNull()
                    if (lastItem == null) {
                        Log.i(TAG, "Page End")
                        return endPaging
                    }
                    lastItem.pageNo.plus(1)
                }
            }
            Log.i(TAG, "Page No, Load Key: $loadKey")

            val response = networkFetch(loadKey+1)

            Log.i(TAG, "Fetch ${response?.size} anime tiles, ${loadType.name}")
            Utils.saveResponse(response, database, loadKey)
            val empty = response?.isEmpty()
            return if (empty == true) {
                endPaging
            } else {
                continuePaging
            }
        } catch (e: IOException) {
            Log.e(TAG, "FAILED Io Error", e)
            MediatorResult.Error(e)
        } catch (e: HttpException) {
            Log.e(TAG, "FAILED Http error", e)
            MediatorResult.Error(e)
        }
    }

    override suspend fun initialize(): InitializeAction {
        val lastUpdate =
            database.frontPageEpisodesDao().lastUpdate()?.minusHours(cacheTimeoutInHours)
        return if (lastUpdate?.isAfter(LocalDateTime.now()) == true) {
            InitializeAction.SKIP_INITIAL_REFRESH
        } else {
            InitializeAction.LAUNCH_INITIAL_REFRESH
        }
    }
}