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

    suspend fun getCurrentPlaytime(episodeSlug: String): Long {
        return db.historyDao().getPlaytime(episodeSlug)
    }

    suspend fun setPlaytime(episodeSlug: String, time: Long) {
        db.historyDao().setPlaytime(episodeSlug, time)
    }

    val recents = Pager(
        PagingConfig(30)
    ) {
        db.historyDao().getLatestWatchedVideos()
    }
}