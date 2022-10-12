package com.otaku.kickassanime.page.animepage

import com.otaku.kickassanime.api.KickassAnimeService
import com.otaku.kickassanime.db.KickassAnimeDb
import com.otaku.kickassanime.db.dao.AnimeEntityDao
import com.otaku.kickassanime.db.dao.EpisodeEntityDao
import com.otaku.kickassanime.db.models.entity.AnimeEntity
import com.otaku.kickassanime.page.adapters.EpisodeAdapter
import com.otaku.kickassanime.utils.asAnimeEntity
import com.otaku.kickassanime.utils.asEpisodeEntities
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject


class AnimeRepository @Inject constructor(private val kickassAnimeDb: KickassAnimeDb,  private val kickassAnimeService: KickassAnimeService) {
    private val animeDao: AnimeEntityDao = kickassAnimeDb.animeEntityDao()
    private val episodeDao: EpisodeEntityDao = kickassAnimeDb.episodeEntityDao()

    fun getAnime(slugId: Int): Flow<AnimeEntity?> {
        return animeDao.getAnimeFlowable(slugId)
    }

    suspend fun invalidateAnime(slug: String) {
        val animeInformation = kickassAnimeService.getAnimeInformation(slug)
        episodeDao.insertAll(animeInformation.asEpisodeEntities())
        val slugId = animeInformation.slugId?.toIntOrNull() ?: return
        val animeEntity = animeDao.getAnime(slugId) ?: return
        animeDao.updateAll(animeInformation.asAnimeEntity(animeEntity))
    }

    fun getEpisodeList(
        animeId: Int
    ): Flow<List<EpisodeAdapter.Episode>> {
        return episodeDao.listEpisodes(animeId)
    }
}
