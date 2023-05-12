package com.otaku.fetch.base.download

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.Download
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

@UnstableApi
@HiltViewModel
class DownloadViewModel @Inject constructor(
    private val downloadRepository: DownloadRepository,
    private val downloadUtils: DownloadUtils
) : ViewModel() {

    private var running = AtomicBoolean(false)

    private val downloadProgressTracker = flow {
        while (running.get()) {
            delay(1000)
            val downloads = downloadUtils.getDownloadManager().downloadIndex.getDownloads(
                Download.STATE_DOWNLOADING
            )
            running.set(downloads.count != 0)
            while (downloads.moveToNext()) {
                emit(downloads.download)
            }
        }
    }

    private val downloadUpdates = object : DownloadTracker.Listener {
        override fun onDownloadsChanged() {
            viewModelScope.launch(Dispatchers.IO) {
                downloadUtils.getDownloadTracker().downloads.let {
                    downloadRepository.findEpisodes(it)
                    running.set(true)
                    downloadProgressTracker.collectLatest { data ->
                        downloadRepository.update(data)
                    }
                }
            }
            refreshDownloadState()
        }

        override fun onIdle() {
            refreshDownloadState()
        }
    }

    fun attach() {
        downloadUtils.getDownloadTracker().attach()
        if (!downloadUtils.getDownloadTracker().hasListener(downloadUpdates)) {
            viewModelScope.launch(Dispatchers.IO) {
                downloadUtils.getDownloadTracker().downloads.let {
                    downloadRepository.findEpisodes(
                        it
                    )
                }
                running.set(true)
                downloadProgressTracker.collectLatest {
                    downloadRepository.update(it)
                }
            }
            downloadUtils.getDownloadTracker().addListener(downloadUpdates)
        }
    }

    fun anime(): DownloadRepository.Root {
        return downloadRepository.root
    }

    fun deleteAnime(anime: DownloadRepository.Anime, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            anime.removeSelf(downloadUtils, context)
        }
    }

    fun deleteEpisode(episode: DownloadRepository.Episode, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            episode.removeSelf(downloadUtils, context)
        }
    }

    fun deleteLink(download: DownloadRepository.Link, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            download.removeSelf(downloadUtils, context)
        }
    }

    fun detachListener() {
        running.set(false)
        downloadUtils.getDownloadTracker().removeListener(downloadUpdates)
    }

    fun pause(context: Context) {
        downloadUtils.getDownloadTracker().pauseDownload(context)
    }

    fun resume(context: Context) {
        downloadUtils.getDownloadTracker().resumeDownload(context)
    }

    fun refreshDownloadState() {
        val downloadManager = downloadUtils.getDownloadManager()
        isDownloadPaused.value = downloadManager.downloadsPaused
    }

    var isDownloadPaused = mutableStateOf(false)
}