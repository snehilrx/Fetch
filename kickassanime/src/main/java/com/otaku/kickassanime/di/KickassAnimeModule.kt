package com.otaku.kickassanime.di

import android.annotation.SuppressLint
import android.content.Context
import androidx.room.Room
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.otaku.kickassanime.api.AnimeSkipService
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
    fun kickassAnimeService(@Named("kickass") retrofit: Retrofit): KickassAnimeService =
        retrofit.create(KickassAnimeService::class.java)


    @Provides
    @Singleton
    fun animeSkipService(@Named("animeskip") retrofit: Retrofit): AnimeSkipService =
        retrofit.create(AnimeSkipService::class.java)


    @Provides
    @Singleton
    fun gson(): Gson = GsonBuilder().setLenient().serializeNulls().create()

    @SuppressLint("UnsafeOptInUsageError")
    @Provides
    @Singleton
    fun kickassDatabase(@ApplicationContext context: Context): KickassAnimeDb =
        Room.databaseBuilder(
            context, KickassAnimeDb::class.java, "kick.db"
        ).fallbackToDestructiveMigration()
            .setAutoCloseTimeout(10000, TimeUnit.DAYS).build()
}