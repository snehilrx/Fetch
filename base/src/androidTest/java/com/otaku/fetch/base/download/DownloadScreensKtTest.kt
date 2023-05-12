package com.otaku.fetch.base.download

import androidx.compose.ui.test.junit4.createComposeRule
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class DownloadScreensKtTest {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createComposeRule()

    @Before
    fun setup() = hiltRule.inject()


    @Test
    fun downloadScreenUpdatesTest() {
        composeTestRule.setContent {
        }
        Thread.sleep(10000)

    }

}