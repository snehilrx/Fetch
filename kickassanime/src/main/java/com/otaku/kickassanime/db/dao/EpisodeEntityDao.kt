package com.otaku.kickassanime.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.otaku.kickassanime.db.models.entity.EpisodeEntity
import com.otaku.kickassanime.page.adapters.EpisodeAdapter
import kotlinx.coroutines.flow.Flow

@Dao
interface EpisodeEntityDao : BaseDao<EpisodeEntity> {

    @Query("SELECT * FROM episode WHERE episodeSlugId = :slugId")
    fun getEpisodeFlow(slugId: Int): Flow<EpisodeEntity?>

    @Query("SELECT * FROM episode WHERE episodeSlugId = :slugId")
    fun getEpisode(slugId: Int): EpisodeEntity?

    @Query("SELECT name as title, episodeSlugId as id FROM episode where animeId is :animeId order by cast(name as unsigned)  desc")
    fun listEpisodes(animeId: Int): Flow<List<EpisodeAdapter.Episode>>
}