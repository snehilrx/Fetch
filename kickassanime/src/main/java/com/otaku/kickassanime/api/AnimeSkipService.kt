package com.otaku.kickassanime.api

import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AnimeSkipService {
    @POST("/graphql")
    suspend fun gql(@Body body: RequestBody): String

    @Suppress("unused")
    @GET("/graphql")
    suspend fun gqlNoOp(@Body body: RequestBody): String

}