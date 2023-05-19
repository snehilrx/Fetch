package com.otaku.kickassanime.api

import com.otaku.kickassanime.api.model.*
import retrofit2.http.*


interface KickassAnimeService {

    @GET("/api/show/recent?type=all")
    suspend fun getFrontPageAnimeList(
        @Query("page") pageNo: Int
    ): RecentApiResponse


    @GET("/api/show/recent?type=sub")
    suspend fun getFrontPageAnimeListSub(
        @Query("page") pageNo: Int
    ): RecentApiResponse


    @GET("/api/show/recent?type=dub")
    suspend fun getFrontPageAnimeListDub(
        @Query("page") pageNo: Int
    ): RecentApiResponse

    @GET("api/show/filters")
    suspend fun getFilters(): Filters

    @POST("/api/fsearch")
    suspend fun search(@Body query: SearchRequest): AnimeSearchResponse

    @POST("/api/search")
    suspend fun searchHints(@Body query: SearchRequest): List<SearchItem>


    @GET("/api/episode/{slug}")
    suspend fun getEpisode(
        @Path("slug") path: String
    ): EpisodeApiResponse

    @GET("/api/show/{slug}/episodes")
    suspend fun getEpisodes(
        @Path("slug") path: String,
        @Query("lang") language: String,
        @Query("page") page: Int
    ): EpisodesResponse?

    @GET("/api/show/{slug}/language")
    suspend fun getLanguage(
        @Path("slug") path: String
    ): BaseApiResponse<String>
}