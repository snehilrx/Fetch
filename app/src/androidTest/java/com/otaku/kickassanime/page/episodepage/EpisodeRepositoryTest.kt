package com.otaku.kickassanime.page.episodepage

import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
class EpisodeRepositoryTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Before
    fun setup() = hiltRule.inject()

    @Inject
    lateinit var episodeRepository: EpisodeRepository

    @Test
    fun testAnimeSkip(){
        runBlocking {
            assertNotNull(episodeRepository.fetchAnimeSkipTime("dragon", 1))
        }
    }
}