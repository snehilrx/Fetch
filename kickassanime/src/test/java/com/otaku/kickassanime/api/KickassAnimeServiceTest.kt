package com.otaku.kickassanime.api

import com.fetch.cloudflarebypass.CloudflareHTTPClient
import com.fetch.cloudflarebypass.Log
import com.google.gson.GsonBuilder
import com.otaku.kickassanime.Strings
import com.otaku.kickassanime.api.conveter.FindJsonInTextConverterFactory
import com.otaku.kickassanime.api.model.SearchRequest
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import org.junit.Assert.assertNotNull
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
                println(s)
            }

            override fun e(tag: String, s: String) {
                println(s)
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

        }
    }

    @Test
    fun testGetNewSeasonAnimeEntries() {
        runBlocking {

        }
    }

    @Test
    fun testGetFrontPageAnimeList() {
        runBlocking {

        }
    }

    @Test
    fun testGetAnimeEpisode() {
        runBlocking {

        }
    }

    @Test
    fun testSearch() {
        runBlocking {
            val results =
                kickassAnimeService.search(SearchRequest("dragon", filters = "{}", page = 1))
            assertNotNull(results)
        }
    }

    @Test
    fun testGetAnimeInformation() {
        runBlocking {
        }
    }

}
