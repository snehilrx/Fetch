package com.otaku.kickassanime.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.otaku.kickassanime.db.models.entity.EpisodeEntity

@Dao
interface EpisodeEntityDao : BaseDao<EpisodeEntity> {

    @Query("SELECT * FROM episode WHERE episodeSlugId = :slugId")
    suspend fun getEpisode(slugId: Int) : EpisodeEntity?
}