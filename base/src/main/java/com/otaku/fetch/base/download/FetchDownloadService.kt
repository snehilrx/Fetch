package com.otaku.fetch.base.download

import android.app.Notification
import android.content.Context
import androidx.media3.common.util.NotificationUtil
import androidx.media3.common.util.Util
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.offline.DownloadNotificationHelper
import androidx.media3.exoplayer.offline.DownloadService
import androidx.media3.exoplayer.scheduler.PlatformScheduler
import androidx.media3.exoplayer.scheduler.Requirements
import androidx.media3.exoplayer.scheduler.Scheduler
import com.otaku.fetch.base.R
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


const val DOWNLOAD_NOTIFICATION_CHANNEL_ID = "download_channel"

@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
@AndroidEntryPoint
class FetchDownloadService : DownloadService(
    FOREGROUND_NOTIFICATION_ID,
    DEFAULT_FOREGROUND_NOTIFICATION_UPDATE_INTERVAL,
    DOWNLOAD_NOTIFICATION_CHANNEL_ID,
    R.string.exo_download_notification_channel_name,
    0
) {

    @Inject
    lateinit var downloadUtils: DownloadUtils

    private lateinit var downloadNotificationHelper: DownloadNotificationHelper

    private lateinit var terminalStateNotification: TerminalStateNotificationHelper

    override fun getDownloadManager(): DownloadManager {
        // This will only happen once, because getDownloadManager is guaranteed to be called only once
        // in the life cycle of the process.
        val downloadManager: DownloadManager = downloadUtils.getDownloadManager()
        downloadUtils.getDownloadTracker().attach()
        return downloadManager
    }

    override fun onCreate() {
        super.onCreate()
        val downloadNotificationHelper: DownloadNotificationHelper =
            getDownloadNotificationHelper( /* context= */this)
        terminalStateNotification = TerminalStateNotificationHelper(
            downloadNotificationHelper,
            FOREGROUND_NOTIFICATION_ID + 1
        )
        downloadManager.addListener(terminalStateNotification)
    }

    override fun onDestroy() {
        downloadManager.removeListener(terminalStateNotification)
        downloadUtils.getDownloadTracker().release()
        super.onDestroy()
    }

    override fun getScheduler(): Scheduler = PlatformScheduler(this, JOB_ID)

    override fun getForegroundNotification(
        downloads: List<Download>, notMetRequirements: @Requirements.RequirementFlags Int
    ): Notification {
        return getDownloadNotificationHelper( /* context= */this)
            .buildProgressNotification(
                this,
                R.drawable.baseline_file_download_24,
                null,
                null,
                downloads,
                notMetRequirements
            )
    }

    /**
     * Creates and displays notifications for downloads when they complete or fail.
     *
     *
     * This helper will outlive the lifespan of a single instance of [FetchDownloadService].
     * It is static to avoid leaking the first [FetchDownloadService] instance.
     */
    private inner class TerminalStateNotificationHelper(
        private val notificationHelper: DownloadNotificationHelper,
        firstNotificationId: Int
    ) : DownloadManager.Listener {

        private var nextNotificationId: Int = firstNotificationId

        override fun onDownloadChanged(
            downloadManager: DownloadManager,
            download: Download,
            finalException: Exception?
        ) {
            val notification: Notification = when (download.state) {
                Download.STATE_COMPLETED -> {
                    notificationHelper.buildDownloadCompletedNotification(
                        this@FetchDownloadService,
                        R.drawable.baseline_file_download_done_24,
                        null,
                        Util.fromUtf8Bytes(download.request.data)
                    )
                }

                Download.STATE_FAILED -> {
                    notificationHelper.buildDownloadFailedNotification(
                        this@FetchDownloadService,
                        com.anggrayudi.storage.R.drawable.mtrl_ic_error,
                        null,
                        Util.fromUtf8Bytes(download.request.data)
                    )
                }

                else -> {
                    return
                }
            }
            NotificationUtil.setNotification(
                this@FetchDownloadService,
                nextNotificationId++,
                notification
            )
        }
    }

    private fun getDownloadNotificationHelper(
        context: Context
    ): DownloadNotificationHelper {
        if (!this::downloadNotificationHelper.isInitialized) {
            downloadNotificationHelper =
                DownloadNotificationHelper(context, DOWNLOAD_NOTIFICATION_CHANNEL_ID)
        }
        return downloadNotificationHelper
    }

    companion object {
        private const val JOB_ID = 1
        private const val FOREGROUND_NOTIFICATION_ID = 1
    }
}