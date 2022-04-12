package com.otaku.kickassanime.api.model

import com.google.gson.annotations.SerializedName


data class Anime (
    @SerializedName("episode"      ) val episode     : String? = null,
    /** anime/boruto-naruto-next-generations-923495/episode-245-958365 */
    @SerializedName("slug"         ) val slug        : String? = null,
    @SerializedName("type"         ) val type        : String? = null,
    @SerializedName("episode_date" ) val episodeDate : String? = null,
    @SerializedName("name"         ) val name        : String? = null,
    @SerializedName("poster"       ) val poster      : String? = null
)

data class AnimeCollection (
    @SerializedName("all") val anime : List<Anime>
)


data class AnimeListFrontPageResponse (
    @SerializedName("data") val anime : AnimeCollection
)