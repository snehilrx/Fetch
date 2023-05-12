package com.otaku.kickassanime.page.frontpage.data

import com.otaku.kickassanime.api.KickassAnimeService
import com.otaku.kickassanime.db.KickassAnimeDb
import com.otaku.kickassanime.utils.Constraints.cacheTimeoutInHours
import com.otaku.kickassanime.utils.Utils
import org.threeten.bp.LocalDateTime
import javax.inject.Inject

class FrontPageRepository @Inject constructor(
    private val database: KickassAnimeDb,
    private val kickassAnimeService: KickassAnimeService
) {

    fun getFirst30RecentItems() = database.recentDao().getRecentPageZero()
    fun getFirst30RecentSubItems() = database.recentDao().getRecentPageZeroSub()
    fun getFirst30RecentDubItems() = database.recentDao().getRecentPageZeroDub()
    suspend fun fetchAll() {
        val dub = kickassAnimeService.getFrontPageAnimeListDub(1).result
        val sub = kickassAnimeService.getFrontPageAnimeListSub(1).result

        database.recentDao().removePage(0)

        Utils.saveRecent(dub, database, 0)
        Utils.saveRecent(sub, database, 0)
    }

    suspend fun lastUpdate(): LocalDateTime? {
        return database.lastUpdateDao().lastUpdate()?.minusHours(cacheTimeoutInHours)
    }
}