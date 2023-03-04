package com.otaku.kickassanime.page.episodepage

import android.util.Log
import com.google.gson.Gson
import com.otaku.fetch.base.TAG
import com.otaku.kickassanime.api.AnimeSkipService
import com.otaku.kickassanime.api.KickassAnimeService
import com.otaku.kickassanime.api.model.Dust
import com.otaku.kickassanime.db.KickassAnimeDb
import com.otaku.kickassanime.db.models.EpisodeAnime
import com.otaku.kickassanime.db.models.Timeline
import com.otaku.kickassanime.db.models.entity.AnimeEntity
import com.otaku.kickassanime.db.models.entity.EpisodeEntity
import com.otaku.kickassanime.utils.asAnimeEntity
import com.otaku.kickassanime.utils.asEpisodeEntity
import kotlinx.coroutines.flow.Flow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

class EpisodeRepository @Inject constructor(
    private val kickassAnimeDb: KickassAnimeDb,
    private val kickassAnimeService: KickassAnimeService,
    private val animeSkip: AnimeSkipService,
    private val gson: Gson
) {

    fun getEpisodeWithAnime(episodeSlugId: Int, animeSlugId: Int): Flow<EpisodeAnime?> {
        return kickassAnimeDb.episodeEntityDao().getEpisodeWithAnime(episodeSlugId, animeSlugId)
    }

    suspend fun fetchRemote(animeSlugId: Int, episodeSlugId: Int) : Pair<EpisodeEntity?, AnimeEntity?>? {
        val episode = kickassAnimeDb.episodeEntityDao().getEpisode(episodeSlugId) ?: return null
        val episodeSlug = episode.episodeSlug ?: return null
        val animeEpisode = kickassAnimeService.getAnimeEpisode(episodeSlug)
        val anime = kickassAnimeDb.animeEntityDao().getAnime(animeSlugId)
        val animeEntity = animeEpisode.anime?.asAnimeEntity(anime)
        animeEntity?.let { kickassAnimeDb.animeEntityDao().updateAll(it) }
        val episodeEntity = animeEpisode.asEpisodeEntity(episode)
        episodeEntity?.let { kickassAnimeDb.episodeEntityDao().insert(it) }
        val episodes = animeEpisode.asEpisodeEntity().filter { it.episodeSlug != episodeSlug }
        kickassAnimeDb.episodeEntityDao().insertAll(episodes)
        if (animeEntity != null) {
            kickassAnimeDb.animeEntityDao().updateAll(animeEntity)
        }
        if (episodeEntity != null) {
            kickassAnimeDb.episodeEntityDao().updateAll(episodeEntity)
        }
        return Pair(episodeEntity, animeEntity)
    }

    suspend fun fetchDustLinks(link: String): Dust? {
        try {
            val text = kickassAnimeService.urlToText(link)
            val find = jsText.find(text)?.value ?: return null
            return gson.fromJson("{\"data\": $find}", Dust::class.java)
        } catch (e: Exception){
            Log.e(TAG, e.message, e)
            return null
        }
    }

    suspend fun fetchAnimeSkipTime(animeName: String, episodeNumber: Int): List<Float?>? {
        val idJson = animeSkip.gql("{\"query\":\"query{searchShows(search:\\\"$animeName\\\",limit:1){id}}\",\"variables\":{}}".toRequestBody(contentType = "application/json".toMediaTypeOrNull()))
        val showId = if(idJson.length < 67) return null else idJson.subSequence(31, 67)
        val timestampJson = animeSkip.gql("{\"query\":\"query{findShow(showId:\\\"$showId\\\"){episodes{timestamps{at}}}}\",\"variables\":{}}".toRequestBody(contentType = "application/json".toMediaTypeOrNull()))
        Log.e(TAG, timestampJson)
        val episodes = gson.fromJson(timestampJson, Timeline::class.java)?.data?.findShow?.episodes ?: return null
        if (episodeNumber >= episodes.size) return null
        return episodes[episodeNumber].timestamps.map {
            it.at
        }
    }

    companion object{
        @JvmStatic
        private val jsText = "\\[\\{.*\\}\\]".toRegex()
    }


}