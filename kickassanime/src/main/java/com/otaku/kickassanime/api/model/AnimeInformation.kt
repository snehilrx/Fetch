package com.otaku.kickassanime.api.model

import com.google.gson.annotations.SerializedName


data class AnimeInformation(
    @SerializedName("anime_id") val animeId: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("en_title") val enTitle: String? = null,
    /** /anime/boruto-naruto-next-generations-923495 */
    @SerializedName("slug") val slug: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("image") val image: String? = null,
    @SerializedName("banner") val banner: String? = null,
    @SerializedName("startdate") val startdate: String? = null,
    @SerializedName("enddate") val enddate: String? = null,
    @SerializedName("broadcast_day") val broadcastDay: String? = null,
    @SerializedName("broadcast_time") val broadcastTime: String? = null,
    @SerializedName("source") val source: String? = null,
    @SerializedName("duration") val duration: String? = null,
    @SerializedName("site") val site: String? = null,
    @SerializedName("info_link") val infoLink: String? = null,
    @SerializedName("createddate") val createddate: String? = null,
    @SerializedName("mal_id") val malId: String? = null,
    @SerializedName("simkl_id") val simklId: String? = null,
    @SerializedName("types") val types: ArrayList<Types>? = arrayListOf(),
    @SerializedName("genres") val genres: ArrayList<Genres>? = arrayListOf(),
    @SerializedName("slug_id") val slugId: String? = null,
    @SerializedName("type") val type: String? = null,
    @SerializedName("episodes") val episodes: ArrayList<Episodes> = arrayListOf(),
    @SerializedName("aid") val aid: String? = null,
    @SerializedName("favorited") val favorited: Boolean? = null,
    @SerializedName("votes") val votes: Int? = null,
    @SerializedName("rating") val rating: Boolean? = null
)