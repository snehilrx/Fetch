package com.otaku.fetch.base.download

import android.content.Context
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.DatabaseProvider
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.cache.Cache
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cronet.CronetDataSource
import androidx.media3.datasource.cronet.CronetUtil
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.offline.DefaultDownloadIndex
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.offline.DownloadRequest
import okhttp3.OkHttpClient
import org.chromium.net.CronetEngine
import java.net.CookieHandler
import java.net.CookieManager
import java.net.CookiePolicy
import java.util.concurrent.Executor
import java.util.concurrent.Executors

@UnstableApi
class DownloadUtils constructor(
    private val okhttp: OkHttpClient,
    private val context: Context,
    private val cache: Cache,
    private val standaloneDatabaseProvider: StandaloneDatabaseProvider
) : PTDownloaderFactory.Tracker {

    /**
     * Whether the demo application uses Cronet for networking. Note that Cronet does not provide
     * automatic support for cookies (https://github.com/google/ExoPlayer/issues/5975).
     *
     *
     * If set to false, the platform's default network stack is used with a [CookieManager]
     * configured in [.getHttpDataSourceFactory].
     */

    private lateinit var downloadManager: DownloadManager
    private lateinit var downloadTracker: DownloadTracker
    private lateinit var httpDataSourceFactory: DataSource.Factory


    var downloadsProgressCallback: PTDownloaderFactory.Tracker? = null

    @Synchronized
    fun getHttpDataSourceFactory(context: Context): DataSource.Factory {
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

                httpDataSourceFactory = OkHttpDataSource.Factory(okhttp)
            }
        }
        return httpDataSourceFactory
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
            downloadManager = getDownloadManager(
                context,
                standaloneDatabaseProvider,
                cache,
                getHttpDataSourceFactory(context),
                Executors.newFixedThreadPool(6)
            )
            downloadTracker =
                DownloadTracker(getHttpDataSourceFactory(context), downloadManager)
        }
    }

    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    fun getDownloadManager(
        context: Context,
        databaseProvider: DatabaseProvider?,
        cache: Cache?,
        upstreamFactory: DataSource.Factory?,
        executor: Executor?
    ) = DownloadManager(
        context,
        DefaultDownloadIndex(databaseProvider!!),
        PTDownloaderFactory(
            CacheDataSource.Factory()
                .setCache(cache!!)
                .setUpstreamDataSourceFactory(upstreamFactory),
            executor!!
        ).apply {
            this.progressHook = this@DownloadUtils
        }
    )

    companion object {
        private const val USE_CRONET_FOR_NETWORKING = false
    }

    override fun onProgressChanged(
        request: DownloadRequest,
        contentLength: Long,
        bytesDownloaded: Long,
        percentDownloaded: Float
    ) {
        downloadsProgressCallback?.onProgressChanged(
            request,
            contentLength,
            bytesDownloaded,
            percentDownloaded
        )
    }
}