package com.otaku.fetch.base.di.modules

import android.content.Context
import android.os.Environment
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.cache.Cache
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import com.fetch.cloudflarebypass.CloudflareHTTPClient
import com.fetch.cloudflarebypass.Log
import com.otaku.fetch.base.download.DownloadUtils
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApplicationModule {

    @Provides
    @Singleton
    fun getHttpLogger() =
        HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.HEADERS }


    @Provides
    @Singleton
    fun getOkHttp(logger: HttpLoggingInterceptor) = CloudflareHTTPClient(object : Log {
        override fun i(tag: String, s: String) {
            android.util.Log.i(tag, s)
        }

        override fun e(tag: String, s: String) {
            android.util.Log.e(tag, s)
        }
    }).okHttpClient.callTimeout(4, TimeUnit.MINUTES)
        .connectTimeout(4, TimeUnit.MINUTES)
        .addInterceptor(logger).build()

    private const val MAX_CACHE_SIZE: Long = 2000000000

    @Provides
    @Singleton
    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    fun getDownloadCache(
        @ApplicationContext context: Context,
        @Named("cache") downloadContentDirectory: File
    ): Cache {
        return SimpleCache(
            downloadContentDirectory,
            LeastRecentlyUsedCacheEvictor(MAX_CACHE_SIZE),
            StandaloneDatabaseProvider(context)
        )
    }

    @Provides
    @Named("cache")
    fun getCacheFolder(@ApplicationContext context: Context): File {
        return context.cacheDir
    }

    @Provides
    @Named("downloads")
    fun getDownloads(): File {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
    }


    @Provides
    @Singleton
    fun getDownloadUtils(
        okhttp: OkHttpClient,
        @ApplicationContext context: Context,
        cache: Cache
    ): DownloadUtils {
        return DownloadUtils(okhttp, context, cache)
    }

}