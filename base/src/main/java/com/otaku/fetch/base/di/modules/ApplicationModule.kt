package com.otaku.fetch.base.di.modules

import  com.fetch.cloudflarebypass.CloudflareHTTPClient
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
        val cloudflareHTTPClient = CloudflareHTTPClient(logger)
        logger.level = HttpLoggingInterceptor.Level.HEADERS
        return cloudflareHTTPClient.okHttpClient
    }

}