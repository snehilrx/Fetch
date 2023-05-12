package com.otaku.fetch.base.download

import android.content.Context
import androidx.media3.database.DatabaseProvider
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.cache.Cache
import androidx.media3.datasource.cronet.CronetDataSource
import androidx.media3.datasource.cronet.CronetUtil
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.offline.DownloadNotificationHelper
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import org.chromium.net.CronetEngine
import java.net.CookieHandler
import java.net.CookieManager
import java.net.CookiePolicy
import java.util.concurrent.Executors

class DownloadUtils constructor(
    private val okhttp: OkHttpClient,
    private val context: Context,
    private val cache: Cache
) {

    /**
     * Whether the demo application uses Cronet for networking. Note that Cronet does not provide
     * automatic support for cookies (https://github.com/google/ExoPlayer/issues/5975).
     *
     *
     * If set to false, the platform's default network stack is used with a [CookieManager]
     * configured in [.getHttpDataSourceFactory].
     */

    private lateinit var databaseProvider: DatabaseProvider
    private lateinit var downloadManager: DownloadManager
    private lateinit var downloadTracker: DownloadTracker
    private lateinit var downloadNotificationHelper: DownloadNotificationHelper
    private lateinit var httpDataSourceFactory: DataSource.Factory

    @Synchronized
    private fun getHttpDataSourceFactory(context: Context): DataSource.Factory {
        if (!this::httpDataSourceFactory.isInitialized) {
            if (USE_CRONET_FOR_NETWORKING) {
                val cronetEngine: CronetEngine? = CronetUtil.buildCronetEngine(context)
                if (cronetEngine != null) {
                    httpDataSourceFactory =
                        CronetDataSource.Factory(
                            cronetEngine,
                            Executors.newSingleThreadExecutor()
                        )
                }
            }
            if (!this::httpDataSourceFactory.isInitialized) {
                // We don't want to use Cronet, or we failed to instantiate a CronetEngine.
                val cookieManager = CookieManager()
                cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ORIGINAL_SERVER)
                CookieHandler.setDefault(cookieManager)

                httpDataSourceFactory = OkHttpDataSource.Factory(okhttp.changeOrigin())
            }
        }
        return httpDataSourceFactory
    }

    @Synchronized
    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    fun getDownloadNotificationHelper(): DownloadNotificationHelper {
        if (!this::downloadNotificationHelper.isInitialized) {
            downloadNotificationHelper =
                DownloadNotificationHelper(context, DOWNLOAD_NOTIFICATION_CHANNEL_ID)
        }
        return downloadNotificationHelper
    }

    @Synchronized
    fun getDownloadManager(): DownloadManager {
        ensureDownloadManagerInitialized()
        return downloadManager
    }

    @Synchronized
    fun getDownloadTracker(): DownloadTracker {
        ensureDownloadManagerInitialized()
        return downloadTracker
    }

    @Synchronized
    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    private fun ensureDownloadManagerInitialized() {
        if (!this::downloadManager.isInitialized) {
            downloadManager = DownloadManager(
                context,
                getDatabaseProvider(context),
                cache,
                getHttpDataSourceFactory(context),
                Executors.newFixedThreadPool(6)
            )
            downloadTracker =
                DownloadTracker(getHttpDataSourceFactory(context), downloadManager)
        }
    }

    @Synchronized
    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    private fun getDatabaseProvider(context: Context): DatabaseProvider {
        if (!this::databaseProvider.isInitialized) {
            databaseProvider = StandaloneDatabaseProvider(context)
        }
        return databaseProvider
    }

    companion object {
        const val DOWNLOAD_NOTIFICATION_CHANNEL_ID = "download_channel"
        private const val USE_CRONET_FOR_NETWORKING = false
    }
}


fun OkHttpClient.changeOrigin(): OkHttpClient {
    return newBuilder().addInterceptor(Interceptor { chain ->
        val original = chain.request()

        val request = original.newBuilder()
            .header("origin", "https://kaavid.com")
            .method(original.method, original.body)
            .build()

        chain.proceed(request)
    }).build()
}