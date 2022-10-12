package com.otaku.kickassanime.page.history

import androidx.paging.Pager
import androidx.paging.PagingConfig
import com.otaku.kickassanime.db.KickassAnimeDb
import com.otaku.kickassanime.db.models.entity.VideoHistory
import javax.inject.Inject

class HistoryRepository @Inject constructor(private val db: KickassAnimeDb) {

    suspend fun addToHistory(videoHistory: VideoHistory) {
        db.historyDao().insert(videoHistory)
    }

    suspend fun getCurrentPlaytime(episodeSlugId: Int): Long {
        return db.historyDao().getPlaytime(episodeSlugId)
    }

    suspend fun setPlaytime(episodeSlugId: Int, time: Long) {
        db.historyDao().setPlaytime(episodeSlugId, time)
    }

    val recents = Pager(
        PagingConfig(30)
    ){
        db.historyDao().getLatestWatchedVideos()
    }
}