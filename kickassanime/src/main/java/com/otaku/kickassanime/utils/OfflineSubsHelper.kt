package com.otaku.kickassanime.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.SingleSampleMediaSource
import com.otaku.kickassanime.api.model.CommonSubtitle
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.internal.commonGet
import java.io.File
import javax.inject.Inject

class OfflineSubsHelper @Inject constructor(
    @ApplicationContext private val context: Context,
    private val okHttp: OkHttpClient
) {

    fun downloadSubs(
        animeSlug: String,
        episodeSlug: String,
        subs: List<CommonSubtitle>
    ) {
        val cacheDirectory = context.cacheDir
        val folderName = "/episode/${animeSlug}/${episodeSlug}"

        val folder = File(cacheDirectory, folderName)
        if (!folder.exists()) {
            val isFolderCreated = folder.mkdirs()
            if (isFolderCreated) {
                // Folder was successfully created
                saveSubs(folder, subs)
            } else {
                // Failed to create the folder
                Log.e(
                    "SUB_DOWNLOAD",
                    "Kickass Anime Error : Subs cache cannot be created "
                )
            }
        } else {
            // Folder already exists
            saveSubs(folder, subs)
        }
    }

    private fun saveSubs(
        folder: File,
        subs: List<CommonSubtitle>
    ) {
        subs.forEach {
            val url = it.getLink().toHttpUrl()
            val request = Request.Builder().url(url)
                .header("origin", "https://kaavid.com")
                .header(
                    "user-agent",
                    "Mozilla/5.0 (iPhone; CPU iPhone OS 16_6 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/16.6 Mobile/15E148 Safari/604.1"
                ).commonGet()
            val response = okHttp.newCall(request.build()).execute()

            val parts = it.getFormat().split('/')
            val suffix = if (parts.size > 1) {
                "${parts[0]}.${parts[1]}"
            } else {
                parts[0]
            }
            val subsFile = File("${folder.absolutePath}/${it.getLanguage()}~${suffix}")
            if (!subsFile.exists()) {
                subsFile.writeText(response.body.string())
            }
        }
    }

    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    fun loadSubs(
        animeSlug: String,
        episodeSlug: String,
        offlineCachingDataSourceFactory: CacheDataSource.Factory
    ): List<MediaSource>? {
        val cacheDirectory = context.cacheDir
        val folderName = "/episode/${animeSlug}/${episodeSlug}"
        val folder = File(cacheDirectory, folderName)

        if (folder.exists()) {
            return folder.listFiles()?.map {
                val nameWithoutExtension = it.nameWithoutExtension.split("~")
                SingleSampleMediaSource.Factory(offlineCachingDataSourceFactory).createMediaSource(
                    MediaItem.SubtitleConfiguration
                        .Builder(Uri.fromFile(it))
                        .setLanguage(nameWithoutExtension[0])
                        .setMimeType("${nameWithoutExtension[1]}/${it.extension}")
                        .setLabel(nameWithoutExtension[0])
                        .build(),
                    C.TIME_UNSET
                )
            }
        }
        return null
    }
}