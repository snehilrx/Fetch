package com.otaku.fetch.base.di.modules

import android.content.Context
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.cache.Cache
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.NoOpCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import com.fetch.cloudflarebypass.CloudflareHTTPClient
import com.fetch.cloudflarebypass.Log
import com.otaku.fetch.base.download.DownloadUtils
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
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
    }).okHttpClient.addInterceptor(Interceptor { chain ->
        val original = chain.request()
        val origin = if ("st1.snapmosaic.com".equals(original.url.host, true)) {
            "https://vidco.pro"
        } else {
            "https://kaavid.com"
        }
        val request = original.newBuilder()
            .header("origin", origin)
            .header(
                "user-agent",
                "Mozilla/5.0 (iPhone; CPU iPhone OS 13_2_3 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.0.3 Mobile/15E148 Safari/604.1"
            )
            .method(original.method, original.body)
            .build()

        chain.proceed(request)
    }).callTimeout(4, TimeUnit.MINUTES)
        .connectTimeout(4, TimeUnit.MINUTES)
        .addInterceptor(logger).build()

    private const val MAX_CACHE_SIZE: Long = 2000000000

    @Provides
    @Singleton
    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    fun getDownloadCache(
        @Named("cache") downloadContentDirectory: File,
        db: StandaloneDatabaseProvider
    ): Cache {
        return SimpleCache(
            downloadContentDirectory,
            LeastRecentlyUsedCacheEvictor(MAX_CACHE_SIZE),
            db
        )
    }

    @Provides
    @Singleton
    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    @Named("unlimited")
    fun getDownloadUnlimitedCache(
        @Named("downloads") downloadContentDirectory: File,
        db: StandaloneDatabaseProvider
    ): Cache {
        return SimpleCache(
            downloadContentDirectory,
            NoOpCacheEvictor(),
            db
        )
    }

    @Provides
    @Singleton
    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    fun getDatabase(@ApplicationContext context: Context) = StandaloneDatabaseProvider(context)

    @Provides
    @Named("cache")
    fun getCacheFolder(@ApplicationContext context: Context): File {
        val folder = File("${context.cacheDir.absolutePath}/stream")
        if (!folder.exists()) {
            folder.mkdirs()
        }
        return folder
    }

    @Provides
    @Named("downloads")
    fun getDownloads(@ApplicationContext context: Context): File {
        val folder = File("${context.cacheDir.absolutePath}/downloads")
        if (!folder.exists()) {
            folder.mkdirs()
        }
        return folder
    }


    @Provides
    @Singleton
    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    fun getDownloadUtils(
        okhttp: OkHttpClient,
        @ApplicationContext context: Context,
        @Named("unlimited") cache: Cache,
        standaloneDatabaseProvider: StandaloneDatabaseProvider
    ): DownloadUtils {
        return DownloadUtils(okhttp, context, cache, standaloneDatabaseProvider)
    }

}