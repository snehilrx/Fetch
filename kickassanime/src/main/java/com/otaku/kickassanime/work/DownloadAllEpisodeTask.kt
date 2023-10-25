package com.otaku.kickassanime.work

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.media3.common.MediaItem
import androidx.media3.common.TrackSelectionParameters
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.offline.DownloadHelper
import androidx.media3.exoplayer.offline.DownloadHelper.Callback
import androidx.media3.exoplayer.offline.DownloadRequest
import androidx.media3.exoplayer.offline.DownloadService
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import com.google.gson.Gson
import com.otaku.fetch.ModuleRegistry
import com.otaku.fetch.base.download.DownloadUtils
import com.otaku.fetch.base.download.FetchDownloadService
import com.otaku.fetch.base.settings.Settings
import com.otaku.fetch.base.settings.dataStore
import com.otaku.kickassanime.Strings
import com.otaku.kickassanime.api.model.CommonSubtitle
import com.otaku.kickassanime.db.models.CommonVideoLink
import com.otaku.kickassanime.page.episodepage.CustomWebView
import com.otaku.kickassanime.page.episodepage.EpisodeViewModel
import com.otaku.kickassanime.pojo.PlayData
import com.otaku.kickassanime.utils.OfflineSubsHelper
import com.otaku.kickassanime.utils.Quality
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.IOException
import kotlin.coroutines.resume


@HiltWorker
class DownloadAllEpisodeTask @AssistedInject constructor(
    @Assisted val context: Context,
    @Assisted val workerParameters: WorkerParameters,
    private val gson: Gson,
    private val downloadUtils: DownloadUtils,
    private val offlineSubsHelper: OfflineSubsHelper
) : CoroutineWorker(context, workerParameters) {


    companion object {
        fun createNewInput(
            episodeUrls: Array<String>, episodeSlugs: Array<String>, animeSlug: String
        ) = Data.Builder().putStringArray(EPISODES_URLS, episodeUrls)
            .putStringArray(EPISODES_SLUGS, episodeSlugs).putString(ANIME_SLUG, animeSlug).build()

        fun getErrors(result: Data): Array<out String>? {
            return result.getStringArray(ERRORS)
        }

        fun getDownloadUrls(result: Data): Array<out String>? {
            return result.getStringArray(DOWNLOAD_URLS)
        }


        private const val EPISODES_URLS = "all_episodes"
        private const val EPISODES_SLUGS = "all_episodes_slugs"
        private const val ANIME_SLUG = "anime_slug"

        private const val DOWNLOAD_URLS = "download_urls"
        private const val ERRORS = "errors"
    }

    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    override suspend fun doWork(): Result {
        return runBlocking {
            val preferences = context.dataStore.data.first()
            // get all episodes
            val episodeUrls = workerParameters.inputData.getStringArray(EPISODES_URLS)
                ?: return@runBlocking Result.failure()
            val episodeSlugs = workerParameters.inputData.getStringArray(EPISODES_SLUGS)
                ?: return@runBlocking Result.failure()
            val animeSlug = workerParameters.inputData.getString(ANIME_SLUG)
                ?: return@runBlocking Result.failure()
            val webView: CustomWebView =
                ModuleRegistry.modules[Strings.KICKASSANIME]?.appModule?.webView as? CustomWebView
                    ?: return@runBlocking Result.failure()

            val allDownloads = ArrayList<Pair<String, PlayData>>()
            loadNextEpisode(webView, episodeUrls, episodeSlugs, 0, allDownloads)
            val downloadRequestUri = ArrayList<String>()
            val errors = ArrayList<String>()
            allDownloads.forEach { (episodeSlug, episodePlayData) ->
                try {
                    val downloadRequest = processEpisode(animeSlug,
                        episodeSlug,
                        episodePlayData,
                        preferences[Settings.DOWNLOADS_VIDEO_QUALITY]?.let { quality ->
                            Quality.entries[quality.toIntOrNull() ?: 0].bitrate
                        } ?: Quality.MAX.bitrate
                    )
                    downloadRequestUri.add(downloadRequest.uri.toString())
                } catch (e: Throwable) {
                    errors.add(episodeSlug)
                }
            }
            Result.success(
                Data.Builder()
                    .putStringArray(DOWNLOAD_URLS, downloadRequestUri.toTypedArray())
                    .putStringArray(ERRORS, errors.toTypedArray())
                    .build()
            )
        }
    }

    @androidx.annotation.OptIn(UnstableApi::class)
    private suspend fun processEpisode(
        animeSlug: String,
        episodeSlug: String,
        playData: PlayData,
        downloadBitRate: Int
    ): DownloadRequest {
        val links = EpisodeViewModel.processPlayData(playData.allSources)
        val subs = ArrayList<CommonSubtitle>()
        val videoLinks = ArrayList<CommonVideoLink>()
        links.forEach { (videoLink, subtitles) ->
            run {
                subs.addAll(subtitles)
                videoLinks.add(videoLink)
            }
        }
        val mediaLink = videoLinks.getOrNull(0)?.getLink() ?: throw Exception("No video link found")
        val trackSelectionParameters =
            TrackSelectionParameters.Builder(context).setPreferredTextLanguage("en")
                .setMinVideoBitrate(downloadBitRate - 1000)
                .setPreferredAudioLanguage("en").setMaxVideoBitrate(downloadBitRate).build()
        offlineSubsHelper.downloadSubs(animeSlug, episodeSlug, subs)
        val link = Uri.parse(mediaLink)
        val helper = DownloadHelper.forMediaItem(
            context,
            MediaItem.fromUri(link),
            DefaultRenderersFactory(context),
            downloadUtils.getHttpDataSourceFactory(context)
        )

        val downloadRequest = suspendCancellableCoroutine<DownloadRequest> { continuation ->
            helper.prepare(object : Callback {
                override fun onPrepared(helper: DownloadHelper) {
                    for (periodIndex in 0 until helper.periodCount) {
                        helper.clearTrackSelections(periodIndex)
                        helper.addTrackSelection(periodIndex, trackSelectionParameters)
                    }
                    val downloadRequest = helper.getDownloadRequest(Util.getUtf8Bytes(episodeSlug))
                    DownloadService.sendAddDownload(
                        context, FetchDownloadService::class.java,
                        downloadRequest, false
                    )
                    continuation.resume(downloadRequest)
                }

                override fun onPrepareError(helper: DownloadHelper, e: IOException) {
                    Log.e("Download Episode", "Failed while preparing media", e)
                    continuation.cancel(e)
                }
            })
        }
        return downloadRequest
    }

    private suspend fun loadNextEpisode(
        webView: CustomWebView,
        episodeUrls: Array<String>,
        episodeSlugs: Array<String>,
        index: Int,
        playlist: ArrayList<Pair<String, PlayData>>
    ) {
        try {
            val enqueue = webView.enqueue(episodeUrls[index])
            val playData = gson.fromJson(enqueue, PlayData::class.java)
            playlist.add(episodeSlugs[index] to playData)
            if (index + 1 < episodeUrls.size) {
                loadNextEpisode(webView, episodeUrls, episodeSlugs, index + 1, playlist)
            }
        } catch (e: Exception) {
            if (index + 1 < episodeUrls.size) {
                loadNextEpisode(webView, episodeUrls, episodeSlugs, index + 1, playlist)
            }
        }
    }

}