package com.otaku.kickassanime.page.episodepage

import android.text.TextUtils
import androidx.room.withTransaction
import com.google.gson.Gson
import com.otaku.kickassanime.api.AnimeSkipService
import com.otaku.kickassanime.api.KickassAnimeService
import com.otaku.kickassanime.db.KickassAnimeDb
import com.otaku.kickassanime.db.models.EpisodeAnime
import com.otaku.kickassanime.db.models.Timeline
import com.otaku.kickassanime.db.models.entity.AnimeEntity
import com.otaku.kickassanime.db.models.entity.EpisodeEntity
import com.otaku.kickassanime.utils.asAnimeEntity
import com.otaku.kickassanime.utils.asEpisodeEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import kotlin.math.abs

class EpisodeRepository @Inject constructor(
    private val kickassAnimeDb: KickassAnimeDb,
    private val kickassAnimeService: KickassAnimeService,
    private val animeSkip: AnimeSkipService,
    private val gson: Gson
) {

    fun getEpisodeWithAnime(episodeSlug: String, animeSlug: String): Flow<EpisodeAnime?> {
        return kickassAnimeDb.episodeEntityDao().getEpisodeWithAnime(episodeSlug, animeSlug)
    }

    suspend fun fetchRemote(
        animeSlug: String,
        episodeSlug: String
    ): Pair<EpisodeEntity?, AnimeEntity?>? {
        val episode = kickassAnimeDb.episodeEntityDao().getEpisode(episodeSlug) ?: return null
        // watch api returns information about anime and episode
        return withContext(Dispatchers.IO) {
            val watchApiResponse = kickassAnimeService.getEpisode(episodeSlug)
            val anime = kickassAnimeDb.withTransaction {
                kickassAnimeDb.animeEntityDao().getAnime(animeSlug)
            }
            val animeFromDb = anime ?: return@withContext null
            if (TextUtils.isEmpty(watchApiResponse.showSlug)) {
                return@withContext null
            }
            val animeEntity = watchApiResponse.asAnimeEntity(animeFromDb.apply {
                this.animeSlug = animeSlug
            })
            val episodeEntity = watchApiResponse.asEpisodeEntity(episode)
            kickassAnimeDb.animeEntityDao().updateAll(animeEntity)
            kickassAnimeDb.episodeEntityDao().updateAll(episodeEntity)
            return@withContext Pair(episodeEntity, animeEntity)
        }
    }

    /**
     * Returns timestamp in seconds
     * */
    suspend fun fetchAnimeSkipTime(
        animeName: String,
        episodeNumber: Float
    ): List<Pair<Long, String?>>? {
        val idJson = animeSkip.gql(
            "{\"query\":\"query{searchShows(search:\\\"$animeName\\\",limit:1){id}}\",\"variables\":{}}".toRequestBody(
                contentType = "application/json".toMediaTypeOrNull()
            )
        )
        val showId = if (idJson.length < 67) return null else idJson.subSequence(31, 67)
        val timestampJson = animeSkip.gql(
            "{\"query\":\"query{findEpisodesByShowId(showId:\\\"$showId\\\"){number,timestamps{at,type{name}}}}\",\"variables\":{}}".toRequestBody(
                contentType = "application/json".toMediaTypeOrNull()
            )
        )
        val episodes =
            gson.fromJson(timestampJson, Timeline::class.java)?.data?.episodes ?: return null
        var closestEpisode = episodes.firstOrNull() ?: return null
        var closestEpisodeNumber = closestEpisode.number?.toFloatOrNull()
        episodes.forEach {
            val currentEpisode = it.number?.toIntOrNull()
            if (closestEpisodeNumber == null || (currentEpisode != null && abs(currentEpisode - episodeNumber) < abs(
                    (closestEpisodeNumber ?: Float.MAX_VALUE) - episodeNumber
                )) && it.timestamps.isNotEmpty()
            ) {
                closestEpisode = it
                closestEpisodeNumber = it.number?.toFloatOrNull()
            }
        }
        return closestEpisode.timestamps.filter { it.at != null }.map {
            Pair(
                it.at!!.toLong() * 1000L,
                it.type.name
            )
        }
    }

    suspend fun fetchLocal(
        animeSlug: String,
        episodeSlug: String
    ): Pair<EpisodeEntity?, AnimeEntity?>? {
        val episode = kickassAnimeDb.episodeEntityDao().getEpisode(episodeSlug) ?: return null
        val anime = kickassAnimeDb.animeEntityDao().getAnime(animeSlug)
        return Pair(episode, anime)
    }
}