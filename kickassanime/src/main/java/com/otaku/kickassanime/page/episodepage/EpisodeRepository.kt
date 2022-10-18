package com.otaku.kickassanime.page.episodepage

import android.util.Log
import com.google.gson.Gson
import com.otaku.fetch.base.TAG
import com.otaku.kickassanime.api.KickassAnimeService
import com.otaku.kickassanime.api.model.Dust
import com.otaku.kickassanime.db.KickassAnimeDb
import com.otaku.kickassanime.db.models.entity.AnimeEntity
import com.otaku.kickassanime.db.models.entity.EpisodeEntity
import com.otaku.kickassanime.utils.asAnimeEntity
import com.otaku.kickassanime.utils.asEpisodeEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class EpisodeRepository @Inject constructor(
    private val kickassAnimeDb: KickassAnimeDb,
    private val kickassAnimeService: KickassAnimeService,
    private val gson: Gson
) {

    fun getAnime(slugId: Int): Flow<AnimeEntity?> {
        return kickassAnimeDb.animeEntityDao().getAnimeFlowable(slugId)
    }

    fun getEpisode(episodeSlugId: Int): Flow<EpisodeEntity?> {
        return kickassAnimeDb.episodeEntityDao().getEpisodeFlow(episodeSlugId)
    }

    suspend fun fetchRemote(animeSlugId: Int, episodeSlugId: Int) : EpisodeEntity? {
        val episode = kickassAnimeDb.episodeEntityDao().getEpisode(episodeSlugId) ?: return null
        val episodeSlug = episode.episodeSlug ?: return null
        val animeEpisode = kickassAnimeService.getAnimeEpisode(episodeSlug)
        animeEpisode.asEpisodeEntity()
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
        return episodeEntity
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

    companion object{
        @JvmStatic
        private val jsText = "\\[\\{.*\\}\\]".toRegex()
    }


}