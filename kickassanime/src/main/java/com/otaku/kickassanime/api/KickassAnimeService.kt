package com.otaku.kickassanime.api

import com.otaku.kickassanime.api.conveter.JsonInText
import com.otaku.kickassanime.api.model.*
import com.otaku.kickassanime.utils.Constraints.NETWORK_PAGE_SIZE
import retrofit2.http.*


interface KickassAnimeService {

    @POST("/api/recent_update?episodeType=all&&perPage=${NETWORK_PAGE_SIZE}")
    suspend fun getFrontPageAnimeList(
        @Query("page") pageNo: Int
    ): List<Anime>


    @POST("/api/recent_update?episodeType=sub&perPage=${NETWORK_PAGE_SIZE}")
    suspend fun getFrontPageAnimeListSub(
        @Query("page") pageNo: Int
    ): List<Anime>


    @POST("/api/recent_update?episodeType=dub&perPage=${NETWORK_PAGE_SIZE}")
    suspend fun getFrontPageAnimeListDub(
        @Query("page") pageNo: Int
    ): List<Anime>

    @GET("/anime-list")
    @JsonInText("animes")
    suspend fun getAllAnimeEntries(): List<AnimeResponse>

    @GET("/new-season")
    @JsonInText("animes")
    suspend fun getNewSeasonAnimeEntries(): List<AnimeResponse>

    @POST("/search")
    @JsonInText("animes")
    suspend fun search(@Query("q") query: String): List<AnimeSearchResponse>

    @GET("{slug}")
    @JsonInText
    suspend fun getAnimeEpisode(
        @Path(
            "slug",
            encoded = true
        ) path: String
    ): AnimeAndEpisodeInformation

    @GET("/api/watch/{slug}")
    suspend fun getAnimeInformation(@Path("slug", encoded = true) path: String): AnimeInformation

    @GET
    suspend fun urlToText(@Url link: String) : String
}