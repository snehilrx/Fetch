package com.otaku.kickassanime.api.model

import com.google.gson.annotations.SerializedName

data class AnimeResponse(
    @SerializedName("anime_id"    ) val animeId     : String? = null,
    @SerializedName("name"        ) val name        : String? = null,
    @SerializedName("slug"        ) val slug        : String? = null,
    @SerializedName("poster"      ) val poster      : String? = null,
    @SerializedName("eps"         ) val eps         : String? = null,
    @SerializedName("description" ) val description : String? = null,
    @SerializedName("filter_id"   ) val filterId    : String? = null,
    @SerializedName("genre_id"    ) val genreId     : String? = null 
)
