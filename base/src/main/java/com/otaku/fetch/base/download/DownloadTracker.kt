package com.otaku.fetch.base.download

import android.content.Context
import android.content.DialogInterface
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.media3.common.*
import androidx.media3.common.util.Util
import androidx.media3.datasource.DataSource
import androidx.media3.exoplayer.RenderersFactory
import androidx.media3.exoplayer.offline.*
import androidx.media3.exoplayer.offline.DownloadHelper.LiveContentUnsupportedException
import com.otaku.fetch.base.R
import com.otaku.fetch.base.media.TrackSelectionDialog
import java.io.IOException
import java.util.concurrent.CopyOnWriteArraySet

@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
class DownloadTracker(
    dataSourceFactory: DataSource.Factory,
    private val downloadManager: DownloadManager
) {
    /** Listens for changes in the tracked downloads.  */
    interface Listener {
        /** Called when the tracked downloads changed.  */
        fun onDownloadsChanged()

        fun onIdle()
    }

    private val dataSourceFactory: DataSource.Factory
    private val listeners: CopyOnWriteArraySet<Listener>
    val downloads: HashMap<Uri, Download>
    private val downloadIndex: DownloadIndex

    private var startDownloadDialogHelper: StartDownloadDialogHelper? = null

    private val downloadListener = DownloadManagerListener()

    init {
        this.dataSourceFactory = dataSourceFactory
        listeners = CopyOnWriteArraySet()
        downloads = HashMap()
        downloadIndex = downloadManager.downloadIndex
        downloadManager.addListener(downloadListener)
        loadDownloads()
    }

    fun addListener(listener: Listener) {
        listeners.add(listener)
    }

    fun removeListener(listener: Listener) {
        listeners.remove(listener)
    }

    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    @Suppress("unused")
    fun isDownloaded(mediaItem: MediaItem): Boolean {
        val download = downloads[checkNotNull(mediaItem.localConfiguration).uri]
        return download?.let {
            it.state != Download.STATE_FAILED
        } ?: false
    }

    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    fun getDownloadRequest(uri: Uri): DownloadRequest? {
        val download = downloads[uri]
        return download?.let {
            if (it.state != Download.STATE_FAILED) it.request else null
        }
    }

    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    fun toggleDownload(
        mediaItem: MediaItem, renderersFactory: RenderersFactory?,
        context: Context
    ) {
        val download = downloads[checkNotNull(mediaItem.localConfiguration).uri]
        if (download != null && download.state != Download.STATE_FAILED) {
            DownloadService.sendRemoveDownload(
                context,
                FetchDownloadService::class.java,
                download.request.id,
                false
            )
        } else {
            startDownloadDialogHelper?.release()
            startDownloadDialogHelper = StartDownloadDialogHelper(
                DownloadHelper.forMediaItem(
                    context,
                    mediaItem,
                    renderersFactory,
                    dataSourceFactory
                ),
                mediaItem,
                context
            )
        }
    }

    fun deleteDownload(download: Download, context: Context) {
        if (download.state != Download.STATE_FAILED) {
            DownloadService.sendRemoveDownload(
                context,
                FetchDownloadService::class.java,
                download.request.id,
                false
            )
        }
    }

    @Suppress("unused")
    fun deleteDownload(uri: Uri, context: Context) {
        val download = downloads[uri]
        if (download != null) {
            deleteDownload(download, context)
        }
    }

    fun resumeDownload(context: Context) {
        DownloadService.sendResumeDownloads(
            context,
            FetchDownloadService::class.java,
            false
        )
    }


    fun pauseDownload(context: Context) {
        DownloadService.sendPauseDownloads(
            context,
            FetchDownloadService::class.java,
            false
        )
    }

    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    private fun loadDownloads() {
        try {
            downloadIndex.getDownloads().use { loadedDownloads ->
                while (loadedDownloads.moveToNext()) {
                    val download = loadedDownloads.download
                    downloads[download.request.uri] = download
                }
            }
        } catch (e: IOException) {
            Log.w(TAG, "Failed to query downloads", e)
        }
    }

    fun release() {
        downloadManager.removeListener(downloadListener)
        listeners.clear()
    }

    fun hasListener(downloadUpdates: Listener): Boolean {
        return listeners.contains(downloadUpdates)
    }

    fun attach() {
        release()
        downloadManager.addListener(downloadListener)
    }

    private inner class DownloadManagerListener : DownloadManager.Listener {
        @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
        override fun onDownloadChanged(
            downloadManager: DownloadManager,
            download: Download,
            finalException: Exception?
        ) {
            downloads[download.request.uri] = download
            for (listener in listeners) {
                listener.onDownloadsChanged()
            }
        }

        @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
        override fun onDownloadRemoved(downloadManager: DownloadManager, download: Download) {
            downloads.remove(download.request.uri)
            for (listener in listeners) {
                listener.onDownloadsChanged()
            }
        }

        override fun onIdle(downloadManager: DownloadManager) {
            super.onIdle(downloadManager)
            for (listener in listeners) {
                listener.onIdle()
            }
        }
    }

    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    private inner class StartDownloadDialogHelper(
        val downloadHelper: DownloadHelper,
        val mediaItem: MediaItem,
        val context: Context
    ) : DownloadHelper.Callback, TrackSelectionDialog.TrackSelectionListener,
        DialogInterface.OnDismissListener {
        private var trackSelectionDialog: TrackSelectionDialog? = null

        init {
            downloadHelper.prepare(this)
        }

        @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
        fun release() {
            downloadHelper.release()
            trackSelectionDialog?.dismiss()
        }

        // DownloadHelper.Callback implementation.
        @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
        override fun onPrepared(helper: DownloadHelper) {
            val format: Format? = getFirstFormatWithDrmInitData(helper)
            if (format == null) {
                onDownloadPrepared(helper)
                return
            }

            // The content is DRM protected. We need to acquire an offline license.
            if (Util.SDK_INT < 18) {
                Toast.makeText(
                    context,
                    R.string.error_drm_unsupported_before_api_18,
                    Toast.LENGTH_LONG
                )
                    .show()
                Log.e(
                    TAG,
                    "Downloading DRM protected content is not supported on API versions below 18"
                )
                return
            }
            if (!hasSchemaData(format.drmInitData)) {
                Toast.makeText(
                    context,
                    R.string.download_start_error_offline_license,
                    Toast.LENGTH_LONG
                )
                    .show()
                Log.e(
                    TAG,
                    "Downloading content where DRM scheme data is not located in the manifest is not"
                            + " supported"
                )
                return
            }
        }

        @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
        override fun onPrepareError(helper: DownloadHelper, e: IOException) {
            val isLiveContent = e is LiveContentUnsupportedException
            val toastStringId: Int =
                if (isLiveContent) R.string.download_live_unsupported else R.string.download_start_error
            val logMessage =
                if (isLiveContent) "Downloading live content unsupported" else "Failed to start download"
            Toast.makeText(context, toastStringId, Toast.LENGTH_LONG).show()
            Log.e(TAG, logMessage, e)
        }

        // TrackSelectionListener implementation.
        @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
        override fun onTracksSelected(trackSelectionParameters: TrackSelectionParameters?) {
            for (periodIndex in 0 until downloadHelper.periodCount) {
                downloadHelper.clearTrackSelections(periodIndex)
                if (trackSelectionParameters != null) {
                    downloadHelper.addTrackSelection(periodIndex, trackSelectionParameters)
                }
            }
            val downloadRequest: DownloadRequest =
                buildDownloadRequest()
            if (downloadRequest.streamKeys.isEmpty()) {
                // All tracks were deselected in the dialog. Don't start the download.
                return
            }
            startDownload(downloadRequest)
        }

        // DialogInterface.OnDismissListener implementation.
        @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
        override fun onDismiss(dialogInterface: DialogInterface) {
            trackSelectionDialog = null
            downloadHelper.release()
        }
        // Internal methods.
        /**
         * Returns the first [Format] with a non-null [Format.drmInitData] found in the
         * content's tracks, or null if none is found.
         */
        @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
        private fun getFirstFormatWithDrmInitData(helper: DownloadHelper): Format? {
            for (periodIndex in 0 until helper.periodCount) {
                val mappedTrackInfo = helper.getMappedTrackInfo(periodIndex)
                for (rendererIndex in 0 until mappedTrackInfo.rendererCount) {
                    val trackGroups = mappedTrackInfo.getTrackGroups(rendererIndex)
                    for (trackGroupIndex in 0 until trackGroups.length) {
                        val trackGroup = trackGroups[trackGroupIndex]
                        for (formatIndex in 0 until trackGroup.length) {
                            val format: Format = trackGroup.getFormat(formatIndex)
                            if (format.drmInitData != null) {
                                return format
                            }
                        }
                    }
                }
            }
            return null
        }

        @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
        private fun onDownloadPrepared(helper: DownloadHelper) {
            if (helper.periodCount == 0) {
                Log.d(TAG, "No periods found. Downloading entire stream.")
                startDownload()
                downloadHelper.release()
                return
            }
            val tracks = downloadHelper.getTracks(0)
            if (!TrackSelectionDialog.willHaveContent(tracks)) {
                Log.d(TAG, "No dialog content. Downloading entire stream.")
                startDownload()
                downloadHelper.release()
                return
            }
            trackSelectionDialog = TrackSelectionDialog.createForTracksAndParameters(
                tracks = tracks,
                DownloadHelper.getDefaultTrackSelectorParameters(context),
                allowAdaptiveSelections = true,
                allowMultipleOverrides = true,
                trackSelectionListener = this,
                onDismissListener = this
            )
            trackSelectionDialog?.show(context)
        }

        /**
         * Returns whether any the [DrmInitData.SchemeData] contained in `drmInitData` has
         * non-null [DrmInitData.SchemeData.data].
         */
        @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
        private fun hasSchemaData(drmInitData: DrmInitData?): Boolean {
            for (i in 0 until (drmInitData?.schemeDataCount ?: -1)) {
                if (drmInitData?.get(i)?.hasData() == true) {
                    return true
                }
            }
            return false
        }

        @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
        private fun startDownload(
            downloadRequest: DownloadRequest = buildDownloadRequest()
        ) {
            DownloadService.sendAddDownload(
                context, FetchDownloadService::class.java, downloadRequest, false
            )
        }

        @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
        private fun buildDownloadRequest(): DownloadRequest {
            return downloadHelper
                .getDownloadRequest(Util.getUtf8Bytes(mediaItem.mediaId))
        }
    }

    companion object {
        private const val TAG = "DownloadTracker"
    }
}