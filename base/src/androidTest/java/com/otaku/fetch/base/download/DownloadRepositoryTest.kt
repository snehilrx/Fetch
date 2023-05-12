package com.otaku.fetch.base.download

import android.app.Activity
import android.net.Uri
import androidx.core.os.bundleOf
import androidx.media3.common.util.Util
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadProgress
import androidx.media3.exoplayer.offline.DownloadRequest
import com.otaku.fetch.AppModule
import com.otaku.fetch.ModuleRegistry
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

const val slug =
    "bofuri-i-dont-want-to-get-hurt-so-ill-max-out-my-defense-season-2-ep9-maxing-defense-and-setting-up-a-base"


@HiltAndroidTest
class DownloadRepositoryTest {


    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Before
    fun setup() = hiltRule.inject()

    @Inject
    lateinit var downloadRepository: DownloadRepository

    private val fakeModule = object : AppModule {
        override val name: String = "fas"

        override suspend fun findEpisode(
            mediaId: String,
            mediaLink: String,
            mediaType: String
        ): DownloadItem {
            return DownloadItem(
                "fake",
                1f,
                "test",
                "asdf",
                bundleOf(),
                Activity::class.java
            )
        }
    }

    @Test
    fun testFindEpisodes() = runTest {
        val fakeDownloads =
            hashMapOf(
                Uri.parse("some") to
                        createFakeDownload(slug)
            )
        ModuleRegistry.registerModule("", 2, fakeModule)
        runBlocking {
            downloadRepository.findEpisodes(fakeDownloads)
            downloadRepository.findEpisodes(fakeDownloads)
            downloadRepository.findEpisodes(fakeDownloads)
        }
        Assert.assertEquals(downloadRepository.root.size, 1)
        val anime = downloadRepository.root.children[0] as DownloadRepository.Anime
        Assert.assertEquals(anime.size, 1)
        val episode = anime.children[0] as DownloadRepository.Episode
        Assert.assertEquals(episode.size, 1)
    }

    private fun createFakeDownload(slug: String): Download {
        val downloadRequest = DownloadRequest.Builder("osna", Uri.parse("kkkk"))
            .setData(Util.getUtf8Bytes(slug)).build()
        return Download(
            downloadRequest,
            Download.STATE_DOWNLOADING,
            0,
            0,
            100,
            Download.FAILURE_REASON_NONE,
            0,
            DownloadProgress()
        )
    }
}