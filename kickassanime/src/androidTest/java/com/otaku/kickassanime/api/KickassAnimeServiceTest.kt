package com.otaku.kickassanime.api

import android.app.Application
import com.otaku.kickassanime.Strings
import dagger.hilt.android.testing.CustomTestApplication
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
@CustomTestApplication(value = Application::class)
class KickassAnimeServiceTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var kickassAnimeService: KickassAnimeService

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun testGetAllAnimeEntries() {
        runBlocking {
            val results = kickassAnimeService.getAllAnimeEntries()
            assertTrue(
                "No anime found in  ${Strings.KICKASSANIME_URL}/anime-list",
                results.isNotEmpty()
            )
        }
    }

    @Test
    fun testGetNewSeasonAnimeEntries() {
        runBlocking {
            val result = kickassAnimeService.getNewSeasonAnimeEntries()
            assertTrue(
                "No anime found in  ${Strings.KICKASSANIME_URL}/anime-list",
                result.isNotEmpty()
            )
        }
    }

    @Test
    fun testGetFrontPageAnimeList() {
        runBlocking {
            val result = kickassAnimeService.getFrontPageAnimeList(0).anime.anime
            assertTrue(
                "No anime found in  ${Strings.KICKASSANIME_URL}/anime-list",
                result.isNotEmpty()
            )
        }
    }

    @Test
    fun testGetAnimeEpisode() {
        runBlocking {
            val episodeSlug = kickassAnimeService.getFrontPageAnimeList(0).anime.anime[0].slug
            assertNotNull("No episode found in page 0", episodeSlug)
            val animeAndEpisodes = kickassAnimeService.getAnimeEpisode(episodeSlug!!)
            assertNotNull(
                "No anime parsed in  ${Strings.KICKASSANIME_URL}/anime-list",
                animeAndEpisodes
            )
            assertNotNull("animeAndEpisodes.anime == null", animeAndEpisodes.anime)
            assertNotNull("animeAndEpisodes.episode == null", animeAndEpisodes.episodeInformation)
            assertNotNull(
                "animeAndEpisodes.anime.animeId == null",
                animeAndEpisodes.anime!!.animeId
            )
        }
    }

    @Test
    fun testSearch(){
        runBlocking {
            val results = kickassAnimeService.search("dragon")
            assertNotNull(results)
            assertTrue("Nothing found for keyword dragon", results.isNotEmpty())
        }
    }

    @Test
    fun testGetAnimeInformation() {
        runBlocking {
            val animeSlug =
                kickassAnimeService.getFrontPageAnimeList(0).anime.anime[0].slug?.dropLastWhile { it != '/' }
            assertNotNull("No episode found in page 0", animeSlug)
            val anime = kickassAnimeService.getAnimeInformation(animeSlug!!)
            assertNotNull("No anime found in  ${Strings.KICKASSANIME_URL}/anime-list", anime)
            assertNotNull("animeAndEpisodes.anime == null", anime.rating)
        }
    }

}
