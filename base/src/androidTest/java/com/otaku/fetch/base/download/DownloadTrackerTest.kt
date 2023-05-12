package com.otaku.fetch.base.download

import android.content.Context
import androidx.media3.datasource.cache.Cache
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import okhttp3.OkHttpClient
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
class DownloadTrackerTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Before
    fun setup() = hiltRule.inject()

    @Inject
    @ApplicationContext
    lateinit var context: Context

    @Inject
    lateinit var okHttpClient: OkHttpClient

    @Inject
    lateinit var cache: Cache

    @Test
    fun testDownloads() {
        val downloadUtils = DownloadUtils(okHttpClient, context, cache)
        Thread.sleep(400)
        Assert.assertTrue(downloadUtils.getDownloadTracker().downloads.isNotEmpty())
    }
}