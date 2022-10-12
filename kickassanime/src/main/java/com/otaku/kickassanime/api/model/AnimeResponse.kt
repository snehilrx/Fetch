package com.otaku.kickassanime.api.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class AnimeResponse(
    @SerializedName("anime_id") val animeId: String? = null,
    @SerializedName("name") val name: String? = null,
    /** example /anime/{slug -> gekidol-alice-in-deadly-school-ova}-{slug-id -> 601016} */
    @SerializedName("slug") val slug: String? = null,
    @SerializedName("poster") val poster: String? = null,
    @SerializedName("eps") val eps: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("filter_id") val filterId: String? = null,
    @SerializedName("genre_id") val genreId: String? = null,
)