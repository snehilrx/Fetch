package com.otaku.kickassanime.page.episodepage

import androidx.test.core.app.ActivityScenario
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import okhttp3.mockwebserver.MockWebServer
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject


@HiltAndroidTest
class EpisodeActivityTest {
    @JvmField
    @Rule
    val hiltAndroidRule = HiltAndroidRule(this)

    @Inject
    lateinit var okHttpClient: MockWebServer

    @Before
    fun setup() {
        hiltAndroidRule.inject()
    }

    @Test
    fun testLifecycle() {

        ActivityScenario.launch(EpisodeActivity::class.java).onActivity {

        }.recreate()


    }

}