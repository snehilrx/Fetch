package com.otaku.kickassanime.di

import android.annotation.SuppressLint
import android.content.Context
import androidx.media3.common.util.UnstableApi
import androidx.room.Room
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.otaku.fetch.AppModule
import com.otaku.kickassanime.PackageModule
import com.otaku.kickassanime.api.KickassAnimeService
import com.otaku.kickassanime.db.KickassAnimeDb
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object KickassAnimeModule {

    @Provides
    @Singleton
    @Named("kickassanime")
    @UnstableApi
    fun kickassAnime(kickassAnimeDb: KickassAnimeDb, kickassAnimeService: KickassAnimeService): AppModule = PackageModule(kickassAnimeService, kickassAnimeDb)

    @Provides
    @Singleton
    fun kickassAnimeService(@Named("kickass") retrofit: Retrofit): KickassAnimeService =
        retrofit.create(KickassAnimeService::class.java)

    @Provides
    @Singleton
    fun gson(): Gson = GsonBuilder().setLenient().serializeNulls().create()

    @SuppressLint("UnsafeOptInUsageError")
    @Provides
    @Singleton
    fun kickassDatabase(@ApplicationContext context: Context): KickassAnimeDb =
        Room.databaseBuilder(
            context, KickassAnimeDb::class.java, "kick.db"
        ).setAutoCloseTimeout(10000, TimeUnit.DAYS).build()
}