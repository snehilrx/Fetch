package com.otaku.kickassanime.page.frontpage.data

import android.util.Log
import androidx.room.withTransaction
import com.otaku.kickassanime.api.KickassAnimeService
import com.otaku.kickassanime.api.model.AnimeListFrontPageResponse
import com.otaku.kickassanime.db.KickassAnimeDb
import com.otaku.kickassanime.utils.Constraints.cacheTimeoutInHours
import com.otaku.kickassanime.utils.Utils.saveResponse
import org.threeten.bp.LocalDateTime
import javax.inject.Inject

class FrontPageRepository @Inject constructor(
    private val database: KickassAnimeDb,
    private val kickassAnimeService: KickassAnimeService
) {

    fun getFrontAllPage() = database.frontPageEpisodesDao().getFirstFrontPageEpisodes()
    fun getFrontSubPage() = database.frontPageEpisodesDao().getFirstFrontPageEpisodesSub()
    fun getFrontDubPage() = database.frontPageEpisodesDao().getFirstFrontPageEpisodesDub()

    suspend fun fetchAll() {
        val sub = kickassAnimeService.getFrontPageAnimeListSub(1)
        val dub = kickassAnimeService.getFrontPageAnimeListDub(1)
        val response = AnimeListFrontPageResponse(sub.anime + dub.anime, 1)
        database.frontPageEpisodesDao().removePage(1)

        Log.i(TAG, "fetchAll: ${response.anime.size} anime fetched")
        saveResponse(response, database)
    }

    suspend fun lastUpdate(): LocalDateTime? {
        return database.frontPageEpisodesDao().lastUpdate()?.minusHours(cacheTimeoutInHours)
    }

    companion object {
        private const val TAG = "FrontPageRepository"
    }
}