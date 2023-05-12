package com.otaku.kickassanime.api.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class EpisodeApiResponse(
    @SerializedName("title") var title: String? = null,
    @SerializedName("title_en") var titleEn: String? = null,
    @SerializedName("synopsis") var synopsis: String? = null,
    @SerializedName("episode_title") var episodeTitle: String? = null,
    @SerializedName("episode_number") var episodeNumber: Int? = null,
    @SerializedName("episode_string") var episodeString: String? = null,
    @SerializedName("language") var language: String? = null,
    @SerializedName("thumbnail") var thumbnail: Images? = null,
    @SerializedName("poster") var poster: Images? = null,
    @SerializedName("banner") var banner: Images? = null,
    @SerializedName("broadcast_day") var broadcastDay: String? = null,
    @SerializedName("broadcast_time") var broadcastTime: String? = null,
    @SerializedName("slug") var slug: String? = null,
    @SerializedName("show_slug") var showSlug: String? = null,
    @SerializedName("servers") var servers: ArrayList<String> = arrayListOf(),
    @SerializedName("next_ep_slug") var nextEpSlug: String? = null,
    @SerializedName("prev_ep_slug") var prevEpSlug: String? = null
)
