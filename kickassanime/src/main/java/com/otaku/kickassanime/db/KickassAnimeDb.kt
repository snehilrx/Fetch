package com.otaku.kickassanime.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.otaku.kickassanime.db.dao.*
import com.otaku.kickassanime.db.models.entity.*

@Database(
    entities = [AnimeEntity::class, EpisodeEntity::class, SearchResultEntity::class,
        AnimeGenreEntity::class, RecentEntity::class, TrendingEntity::class, EpisodePageEntity::class,
        VideoHistory::class, PopularEntity::class, AnimeLanguageEntity::class],
    version = 8,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class KickassAnimeDb : RoomDatabase() {
    abstract fun animeEntityDao(): AnimeEntityDao
    abstract fun episodeEntityDao(): EpisodeEntityDao
    abstract fun animeGenreDao(): AnimeGenreDao
    abstract fun recentDao(): RecentDao
    abstract fun historyDao(): HistoryDao
    abstract fun favouritesDao(): FavouriteDao
    abstract fun animeLanguageDao(): AnimeLanguageDao
    abstract fun episodePageDao(): EpisodePageDao
    abstract fun lastUpdateDao(): LastUpdateDao
    abstract fun searchDao(): SearchResultEntityDao
}