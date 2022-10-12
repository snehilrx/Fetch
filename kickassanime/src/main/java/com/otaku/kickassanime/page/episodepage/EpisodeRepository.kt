package com.otaku.kickassanime.page.episodepage

import android.util.Log
import com.otaku.kickassanime.api.KickassAnimeService
import com.otaku.kickassanime.db.KickassAnimeDb
import com.otaku.kickassanime.db.models.entity.AnimeEntity
import com.otaku.kickassanime.db.models.entity.EpisodeEntity
import com.otaku.kickassanime.utils.asAnimeEntity
import com.otaku.kickassanime.utils.asEpisodeEntity
import javax.inject.Inject

class EpisodeRepository @Inject constructor(
    private val kickassAnimeDb: KickassAnimeDb,
    private val kickassAnimeService: KickassAnimeService
    ) {

    suspend fun getAnime(slugId: Int): AnimeEntity? {
        val animeEntity = kickassAnimeDb.animeEntityDao().getAnime(slugId) ?: return null
        if(animeEntity.description.isNullOrEmpty()) {
            Log.i(TAG, "getAnimeFromEpisodeSlug: episode is not fully fetched")
            val animeSlug = animeEntity.animeslug
            if(animeSlug == null) {
                Log.i(TAG, "getAnimeFromEpisodeSlug: anime slug is null")
                return null
            }
            val response = kickassAnimeService.getAnimeInformation(animeSlug).asAnimeEntity(animeEntity)
            Log.d(TAG, "getAnimeFromEpisodeSlug: response: $response")
            kickassAnimeDb.animeEntityDao().insert(response)
            return response
        }
        return animeEntity
    }

    suspend fun getEpisode(id: Int, animeId: Int): EpisodeEntity? {
        val episode = kickassAnimeDb.episodeEntityDao().getEpisode(id) ?: return null
        val anime = getAnime(animeId) ?: return null
        if(episode.link1.isNullOrEmpty()) {
            Log.i(TAG, "getEpisode: episode is null")
            if (episode.episodeSlug == null) {
                Log.i(TAG, "getEpisode: episode slug is null")
                return null
            }
            val animeEpisode = kickassAnimeService.getAnimeEpisode(episode.episodeSlug)
            val animeEntity = animeEpisode.anime?.asAnimeEntity(anime)
            animeEntity?.let { kickassAnimeDb.animeEntityDao().updateAll(it) }
            val episodeEntity = animeEpisode.asEpisodeEntity(episode)
            episodeEntity?.let { kickassAnimeDb.episodeEntityDao().insert(it) }
            return episodeEntity
        }
        return episode
    }

    companion object {
        private const val TAG = "EpisodeRepository"
    }
}