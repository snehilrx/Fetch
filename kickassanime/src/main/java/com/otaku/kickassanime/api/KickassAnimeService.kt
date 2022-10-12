package com.otaku.kickassanime.api

import com.otaku.kickassanime.api.conveter.JsonInText
import com.otaku.kickassanime.api.model.*
import retrofit2.http.*


interface KickassAnimeService {

    @POST("/api/get_anime_list/all/{pageNo}")
    suspend fun getFrontPageAnimeList(
        @Path("pageNo") pageNo: Int
    ): AnimeListFrontPageResponse


    @POST("/api/get_anime_list/sub/{pageNo}")
    suspend fun getFrontPageAnimeListSub(
        @Path("pageNo") pageNo: Int
    ): AnimeListFrontPageResponse


    @POST("/api/get_anime_list/dub/{pageNo}")
    suspend fun getFrontPageAnimeListDub(
        @Path("pageNo") pageNo: Int
    ): AnimeListFrontPageResponse

    @GET("/anime-list")
    @JsonInText("animes")
    suspend fun getAllAnimeEntries(): List<AnimeResponse>

    @GET("/new-season")
    @JsonInText("animes")
    suspend fun getNewSeasonAnimeEntries(): List<AnimeResponse>

    @FormUrlEncoded
    @POST("/api/anime_search")
    suspend fun search(@Field("keyword") query: String): List<AnimeSearchResponse>

    @GET("{slug}")
    @JsonInText
    suspend fun getAnimeEpisode(@Path("slug", encoded = true) path: String): AnimeAndEpisodeInformation

    @GET("{slug}")
    @JsonInText("anime")
    suspend fun getAnimeInformation(@Path("slug", encoded = true) path: String): AnimeInformation
}