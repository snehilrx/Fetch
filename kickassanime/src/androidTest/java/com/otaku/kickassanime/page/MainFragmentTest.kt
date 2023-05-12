package com.otaku.kickassanime.page

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
//        launchFragmentInContainer<> {
//            it.moveToState(Lifecycle.State.RESUMED)
//            onView(withId(R.id.container))
//        }
    }
}