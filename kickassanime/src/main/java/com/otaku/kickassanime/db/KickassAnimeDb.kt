package com.otaku.kickassanime.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.otaku.kickassanime.db.dao.*
import com.otaku.kickassanime.db.models.entity.*
import org.jetbrains.annotations.NotNull

@Database(
    entities = [AnimeEntity::class, EpisodeEntity::class, AnimeFilter::class, AnimeGenre::class, FrontPageEpisodes::class, VideoHistory::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class KickassAnimeDb : RoomDatabase() {

    abstract fun animeEntityDao(): AnimeEntityDao
    abstract fun episodeEntityDao(): EpisodeEntityDao
    abstract fun animeFilterDao(): AnimeFilterDao
    abstract fun animeGenreDao(): AnimeGenreDao
    abstract fun frontPageEpisodesDao(): FrontPageEpisodesDao
    abstract fun historyDao(): HistoryDao
    abstract fun favouritesDao(): FavouriteDao

    companion object {
        @JvmStatic
        private lateinit var instance: KickassAnimeDb

        fun getInstance(@NotNull context: Context): KickassAnimeDb {
            if (!this::instance.isInitialized) {
                instance = Room.databaseBuilder(
                    context, KickassAnimeDb::class.java, "database-name"
                ).build()
            }
            return instance
        }
    }
}