package com.otaku.fetch.base.download

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.offline.DownloadRequest
import androidx.media3.exoplayer.offline.DownloadService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

@UnstableApi
@HiltViewModel
class DownloadViewModel @Inject constructor(
    private val downloadRepository: DownloadRepository,
    private val downloadUtils: DownloadUtils,
    @Named("io") private val io: CoroutineDispatcher,
) : ViewModel() {

    private val downloadUpdates = object : DownloadManager.Listener {
        override fun onDownloadChanged(
            downloadManager: DownloadManager,
            download: Download,
            finalException: Exception?
        ) {
            downloadRepository.update(download)
        }

        override fun onDownloadRemoved(downloadManager: DownloadManager, download: Download) {
            downloadRepository.update(download)
        }
    }


    fun attach() {
        viewModelScope.launch(io) {
            downloadRepository.findEpisodes(downloadUtils.getDownloadTracker().downloads)
            downloadUtils.downloadsProgressCallback = object : PTDownloaderFactory.Tracker {
                override fun onProgressChanged(
                    request: DownloadRequest,
                    contentLength: Long,
                    bytesDownloaded: Long,
                    percentDownloaded: Float
                ) {
                    downloadRepository.update(
                        request.id,
                        contentLength,
                        bytesDownloaded,
                        percentDownloaded
                    )
                }
            }
        }
        downloadUtils.getDownloadManager().addListener(downloadUpdates)
    }

    fun anime(): DownloadRepository.Root {
        return downloadRepository.root
    }

    fun deleteAnime(anime: DownloadRepository.Anime, context: Context) {
        viewModelScope.launch(io) {
            downloadRepository.delete(anime, downloadUtils, context)
        }
    }

    fun deleteEpisode(episode: DownloadRepository.Episode, context: Context) {
        viewModelScope.launch(io) {
            downloadRepository.delete(episode, downloadUtils, context)
        }
    }

    fun deleteLink(download: DownloadRepository.Link, context: Context) {
        viewModelScope.launch(io) {
            downloadRepository.delete(download, downloadUtils, context)
        }
    }

    public override fun onCleared() {
        downloadUtils.downloadsProgressCallback = null
        downloadUtils.getDownloadManager().removeListener(downloadUpdates)
        super.onCleared()
    }

    fun pause(context: Context) {
        downloadUtils.getDownloadTracker().pauseDownload(context)
    }

    fun resume(context: Context) {
        downloadUtils.getDownloadTracker().resumeDownload(context)
    }

    fun pause(link: DownloadRepository.Link, applicationContext: Context?) {
        applicationContext?.let {
            DownloadService.sendSetStopReason(
                it,
                FetchDownloadService::class.java,
                link.download.download.value.request.id,
                1,
                false
            )
        }
    }

    fun resume(link: DownloadRepository.Link, applicationContext: Context?) {
        applicationContext?.let {
            DownloadService.sendSetStopReason(
                it,
                FetchDownloadService::class.java,
                link.download.download.value.request.id,
                Download.STOP_REASON_NONE,
                false
            )
        }
    }

    fun retry(link: DownloadRepository.Link, applicationContext: Context?) {
        applicationContext?.let {
            DownloadService.sendAddDownload(
                it,
                FetchDownloadService::class.java,
                link.download.download.value.request,
                false
            )
        }
    }

    var isDownloadPaused = mutableStateOf(downloadUtils.getDownloadManager().downloadsPaused)
}