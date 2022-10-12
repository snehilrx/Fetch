package com.otaku.fetch.base.di.modules

import com.fetch.cloudflarebypass.CloudflareHTTPClient
import com.fetch.cloudflarebypass.Log
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.logging.HttpLoggingInterceptor
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApplicationModule {

    @Provides
    @Singleton
    fun getHttpLogger() = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.HEADERS }


    @Provides
    @Singleton
    fun getOkHttp(logger: HttpLoggingInterceptor) = CloudflareHTTPClient(object : Log{
        override fun i(tag: String, s: String) {
            android.util.Log.i(tag, s)
        }

        override fun e(tag: String, s: String) {
            android.util.Log.e(tag, s)
        }

    }).okHttpClient.addInterceptor(logger).build()

}