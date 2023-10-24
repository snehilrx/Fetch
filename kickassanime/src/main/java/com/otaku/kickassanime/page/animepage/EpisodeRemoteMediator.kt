package com.otaku.kickassanime.page.animepage

import android.util.Log
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import com.otaku.fetch.base.TAG
import com.otaku.kickassanime.api.KickassAnimeService
import com.otaku.kickassanime.db.KickassAnimeDb
import com.otaku.kickassanime.utils.Constants
import com.otaku.kickassanime.utils.Utils
import org.threeten.bp.Duration
import org.threeten.bp.LocalDateTime
import retrofit2.HttpException
import java.io.IOException

data class EpisodeTile(
    val pageNo: Int,
    var episodeNumber: Int? = null,
    var thumbnail: String? = null,
    val slug: String? = null,
    val title: String? = null,
    val duration: Long? = null
) {
    fun readableDuration(): String {
        return "${duration?.let { Duration.ofMillis(it).toMinutes() }} min"
    }
}

@OptIn(ExperimentalPagingApi::class)
class EpisodeRemoteMediator(
    private val animeSlug: String,
    private val language: String,
    private val startPage: Int,
    private val api: KickassAnimeService,
    private val database: KickassAnimeDb
) : RemoteMediator<Int, EpisodeTile>() {

    override suspend fun load(
        loadType: LoadType, state: PagingState<Int, EpisodeTile>
    ): MediatorResult {
        return try {
            val endPaging = MediatorResult.Success(endOfPaginationReached = true)
            val continuePaging = MediatorResult.Success(endOfPaginationReached = false)
            val loadKey = when (loadType) {
                LoadType.REFRESH -> startPage
                LoadType.PREPEND -> return endPaging
                LoadType.APPEND -> {
                    val lastItem = state.lastItemOrNull()?.pageNo ?: startPage
                    lastItem.minus(1)
                }
            }
            if (loadKey <= 0) {
                return endPaging
            }
            Log.i(TAG, "Page No, Load Key: $loadKey")

            val response = api.getEpisodes(animeSlug, language, loadKey)

            Log.i(TAG, "Fetch ${response?.result?.size} episodes, ${loadType.name}")
            return if (response?.result.isNullOrEmpty() || response == null) {
                endPaging
            } else {
                Utils.saveEpisodePage(animeSlug, language, response, database, loadKey)
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
        val lastUpdate = database.lastUpdateDao().lastUpdate()
            ?.minusHours(Constants.cacheTimeoutInHours)
        return if (lastUpdate?.isAfter(LocalDateTime.now()) == true) {
            InitializeAction.SKIP_INITIAL_REFRESH
        } else {
            InitializeAction.LAUNCH_INITIAL_REFRESH
        }
    }
}
