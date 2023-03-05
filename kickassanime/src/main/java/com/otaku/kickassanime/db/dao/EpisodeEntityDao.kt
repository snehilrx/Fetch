package com.otaku.kickassanime.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.otaku.kickassanime.db.models.EpisodeAnime
import com.otaku.kickassanime.db.models.entity.EpisodeEntity
import com.otaku.kickassanime.page.adapters.EpisodeAdapter
import kotlinx.coroutines.flow.Flow

@Dao
interface EpisodeEntityDao : BaseDao<EpisodeEntity> {

    @Query("SELECT e.name as _name, e.title as _title, e.episodeSlug as _episodeSlug," +
            " e.episodeSlugId as _episodeSlugId, e.dub as _dub, e.link1 as _link1, e.link2 as _link2," +
            " e.link3 as _link3, e.link4 as _link4, e.animeId as _animeId, e.sector as _sector," +
            " e.createdDate as _createdDate, e.next as _next, e.prev as _prev, e.episodeId as _episodeId," +
            " e.rating as _rating, e.votes as _votes, e.favourite as _favourite, a.* FROM episode as e," +
            " anime as a WHERE episodeSlugId = :slugId and  animeSlugId = :animeSlugId; ")
    fun getEpisodeWithAnime(slugId: Int, animeSlugId: Int): Flow<EpisodeAnime?>

    @Query("SELECT * FROM episode WHERE episodeSlugId = :slugId")
    fun getEpisode(slugId: Int): EpisodeEntity?

    @Query("SELECT name as title, episodeSlugId as id FROM episode where animeId is :animeId order by cast(name as unsigned)  desc")
    fun listEpisodes(animeId: String): Flow<List<EpisodeAdapter.Episode>>

}