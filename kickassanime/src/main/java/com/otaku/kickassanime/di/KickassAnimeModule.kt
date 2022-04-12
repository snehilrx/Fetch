package com.otaku.kickassanime.di

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.otaku.kickassanime.Strings
import com.otaku.kickassanime.api.KickassAnimeService
import com.otaku.kickassanime.api.conveter.FindJsonInTextConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object KickassAnimeModule {

    @Provides
    @Singleton
    fun retrofit(gson: Gson, okHttpClient: OkHttpClient): Retrofit = Retrofit.Builder()
        .addConverterFactory(FindJsonInTextConverterFactory.create(gson))
        .addConverterFactory(GsonConverterFactory.create(gson))
        .client(okHttpClient)
        .baseUrl(Strings.KICKASSANIME_URL)
        .build()

    @Provides
    @Singleton
    fun kickassanimeService(retrofit: Retrofit): KickassAnimeService =
        retrofit.create(KickassAnimeService::class.java)

    @Provides
    @Singleton
    fun gson(): Gson = GsonBuilder().serializeNulls().create()

}