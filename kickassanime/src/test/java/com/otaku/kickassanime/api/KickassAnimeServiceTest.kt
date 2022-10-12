package com.otaku.kickassanime.api

import com.fetch.cloudflarebypass.CloudflareHTTPClient
import com.fetch.cloudflarebypass.Log
import com.google.gson.GsonBuilder
import com.otaku.kickassanime.Strings
import com.otaku.kickassanime.api.conveter.FindJsonInTextConverterFactory
import kotlinx.coroutines.runBlocking
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class KickassAnimeServiceUnitTest {

    private lateinit var okHttpClient: OkHttpClient
    private lateinit var kickassAnimeService: KickassAnimeService

    @Before
    fun setup() {
        okHttpClient = CloudflareHTTPClient(object : Log {
            override fun i(tag: String, s: String) {
                println(s);
            }

            override fun e(tag: String, s: String) {
                println(s);
            }

        }).okHttpClient.build()
        kickassAnimeService = Retrofit.Builder()
            .addConverterFactory(FindJsonInTextConverterFactory.create(GsonBuilder().setLenient().serializeNulls().create()))
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().setLenient().serializeNulls().create()))
            .client(okHttpClient)
            .baseUrl(Strings.KICKASSANIME_URL)
            .build()
            .create(KickassAnimeService::class.java)
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
            val result = kickassAnimeService.getFrontPageAnimeList(1).anime
            assertTrue(
                "No anime found in  ${Strings.KICKASSANIME_URL}/anime-list",
                result.isNotEmpty()
            )
        }
    }

    @Test
    fun testGetAnimeEpisode() {
        runBlocking {
            val episodeSlug = kickassAnimeService.getFrontPageAnimeList(0).anime.get(0).slug
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
                animeAndEpisodes.anime?.animeId
            )
        }
    }

    @Test
    fun testSearch() {
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
                kickassAnimeService.getFrontPageAnimeList(0).anime[0].slug?.dropLastWhile { it != '/' }
            assertNotNull("No episode found in page 0", animeSlug)
            val anime = kickassAnimeService.getAnimeInformation(animeSlug!!)
            assertNotNull("No anime found in  ${Strings.KICKASSANIME_URL}/anime-list", anime)
            assertNotNull("animeAndEpisodes.anime == null", anime.rating)
        }
    }

}
