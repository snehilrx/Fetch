package com.otaku.base.di.modules

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApplicationModule {

    @Provides
    @Singleton
    fun getOkHttp() : OkHttpClient {
        val logger = HttpLoggingInterceptor()
        logger.level = HttpLoggingInterceptor.Level.HEADERS
        return OkHttpClient.Builder().addInterceptor(logger)
            .build()
    }

}