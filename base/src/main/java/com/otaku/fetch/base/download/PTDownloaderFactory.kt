package com.otaku.fetch.base.download

import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.exoplayer.offline.DefaultDownloaderFactory
import androidx.media3.exoplayer.offline.DownloadRequest
import androidx.media3.exoplayer.offline.Downloader
import java.util.concurrent.Executor

@UnstableApi
class PTDownloaderFactory : DefaultDownloaderFactory {

    var progressHook: Tracker? = null

    constructor(cacheDataSourceFactory: CacheDataSource.Factory, executor: Executor) : super(
        cacheDataSourceFactory,
        executor
    )

    class DownloaderWrapper(
        private val downloader: Downloader,
        private val intercept: Downloader.ProgressListener?
    ) : Downloader {
        override fun download(progressListener: Downloader.ProgressListener?) {
            downloader.download { contentLength, bytesDownloaded, percentDownloaded ->
                progressListener?.onProgress(contentLength, bytesDownloaded, percentDownloaded)
                intercept?.onProgress(contentLength, bytesDownloaded, percentDownloaded)
            }
        }

        override fun cancel() {
            downloader.cancel()
        }

        override fun remove() {
            downloader.remove()
        }
    }

    override fun createDownloader(request: DownloadRequest): Downloader {
        return DownloaderWrapper(super.createDownloader(request)) { contentLength, bytesDownloaded, percentDownloaded ->
            progressHook?.onProgressChanged(
                request,
                contentLength,
                bytesDownloaded,
                percentDownloaded
            )
        }
    }

    interface Tracker {
        fun onProgressChanged(
            request: DownloadRequest,
            contentLength: Long,
            bytesDownloaded: Long,
            percentDownloaded: Float
        )
    }
}