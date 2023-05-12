package com.otaku.kickassanime.api.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName


@Keep
class AnimeSearchResponse : BaseApiResponse<SearchItem>()

@Keep
data class SearchItem(
    @SerializedName("genres") var genres: ArrayList<String> = arrayListOf(),
    @SerializedName("locales") var locales: ArrayList<String> = arrayListOf(),
    @SerializedName("episode_count") var episodeCount: Int? = null,
    @SerializedName("slug") var slug: String? = null,
    @SerializedName("status") var status: String? = null,
    @SerializedName("synopsis") var synopsis: String? = null,
    @SerializedName("title") var titleText: String? = null,
    @SerializedName("title_en") var titleEn: String? = null,
    @SerializedName("type") var type: String? = null,
    @SerializedName("year") var year: Int? = null,
    @SerializedName("poster") var poster: Images? = null,
    @SerializedName("episode_duration") var episodeDuration: Int? = null,
    @SerializedName("watch_uri") var watchUri: String? = null,
    @SerializedName("episode_number") var episodeNumber: Int? = null,
    @SerializedName("episode_string") var episodeString: String? = null,
    @SerializedName("rating") var rating: String? = null,

    )