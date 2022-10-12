package com.otaku.kickassanime.page

import androidx.lifecycle.Lifecycle
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.otaku.kickassanime.R
import com.otaku.kickassanime.page.adapters.HeaderAdapter
import com.otaku.kickassanime.utils.launchFragmentInContainer
import com.otaku.kickassanime.utils.readFromFile
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject


@HiltAndroidTest
class MainFragmentTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Before
    fun setup() = hiltRule.inject()

    @Inject
    lateinit var mockWebServer: MockWebServer

    @Test
    fun testRecyclerViewScrollSave() {
        readFromFile("response.txt")?.forEachLine {
            mockWebServer.enqueue(MockResponse().setBody(it))
        }
        launchFragmentInContainer<MainFragment> {
            it.moveToState(Lifecycle.State.RESUMED)
            onView(withId(R.id.container))
        }
    }
}