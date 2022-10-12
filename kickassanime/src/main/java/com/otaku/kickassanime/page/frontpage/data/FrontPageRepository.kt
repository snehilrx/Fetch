package com.otaku.kickassanime.page.frontpage.data

import android.util.Log
import com.otaku.kickassanime.api.KickassAnimeService
import com.otaku.kickassanime.db.KickassAnimeDb
import com.otaku.kickassanime.utils.Constraints.cacheTimeoutInHours
import com.otaku.kickassanime.utils.Utils.saveResponse
import org.threeten.bp.LocalDateTime
import javax.inject.Inject

class FrontPageRepository @Inject constructor(
    private val database: KickassAnimeDb,
    private val kickassAnimeService: KickassAnimeService
) {

    fun getFrontAllPage() = database.frontPageEpisodesDao().getFrontPageEpisodesUpto(20)
    fun getFrontSubPage() = database.frontPageEpisodesDao().getFrontPageEpisodesSubUpto(20)
    fun getFrontDubPage() = database.frontPageEpisodesDao().getFrontPageEpisodesDubUpto(20)

    suspend fun fetchAll() {
        val response = kickassAnimeService.getFrontPageAnimeList(1)
        Log.i(TAG, "fetchAll: ${response.anime.size} anime fetched")
        saveResponse(response, database)
    }

    suspend fun fetchDub() {
        val response = kickassAnimeService.getFrontPageAnimeListSub(1)
        Log.i(TAG, "fetchDub: ${response.anime.size} anime fetched")
        saveResponse(response, database)
    }

    suspend fun fetchSub() {
        val response = kickassAnimeService.getFrontPageAnimeListDub(1)
        Log.i(TAG, "fetchSub: ${response.anime.size} anime fetched")
        saveResponse(response, database)
    }


    suspend fun lastUpdate(): LocalDateTime? {
        return database.frontPageEpisodesDao().lastUpdate()?.minusHours(cacheTimeoutInHours)
    }

    companion object {
        private const val TAG = "FrontPageRepository"
    }
}