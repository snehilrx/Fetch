package com.otaku.kickassanime.page.frontpage.data

import android.util.Log
import com.otaku.kickassanime.api.KickassAnimeService
import com.otaku.kickassanime.api.model.Anime
import com.otaku.kickassanime.db.KickassAnimeDb
import com.otaku.kickassanime.utils.Constraints.cacheTimeoutInHours
import com.otaku.kickassanime.utils.Utils.saveResponse
import org.threeten.bp.LocalDateTime
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

class FrontPageRepository @Inject constructor(
    private val database: KickassAnimeDb,
    private val kickassAnimeService: KickassAnimeService
) {

    fun getFrontAllPage() = database.frontPageEpisodesDao().getFirstFrontPageEpisodes()
    fun getFrontSubPage() = database.frontPageEpisodesDao().getFirstFrontPageEpisodesSub()
    fun getFrontDubPage() = database.frontPageEpisodesDao().getFirstFrontPageEpisodesDub()

    suspend fun fetchAll() {
        val sub = kickassAnimeService.getFrontPageAnimeListSub(0)
        val dub = kickassAnimeService.getFrontPageAnimeListDub(0)
        val response = ArrayList<Anime>()
        response.addAll(sub)
        response.addAll(dub)
        database.frontPageEpisodesDao().removePage(1)

        Log.i(TAG, "fetchAll: ${response.size} anime fetched")
        saveResponse(response, database, 1)
    }

    suspend fun lastUpdate(): LocalDateTime? {
        return database.frontPageEpisodesDao().lastUpdate()?.minusHours(cacheTimeoutInHours)
    }

    companion object {
        private const val TAG = "FrontPageRepository"
    }
}