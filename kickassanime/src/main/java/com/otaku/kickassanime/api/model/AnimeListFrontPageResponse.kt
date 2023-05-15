package com.otaku.kickassanime.api.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
class RecentApiResponse : BaseApiResponse<Recent>()

@Keep
data class Recent(
    @SerializedName("language") var language: String? = null,
    var duration: Long = 0,
    @SerializedName("created_at") var createdAt: String? = null,
    @SerializedName("locales") var locales: ArrayList<String> = arrayListOf(),
    @SerializedName("title_en") var title: String? = null,
    @SerializedName("synopsis") var synopsis: String? = null,
    @SerializedName("episode_title") var episodeTitle: String? = null,
    @SerializedName("episode_number") var episodeNumber: Float? = null,
    @SerializedName("episode_string") var episodeString: String? = null,
    @SerializedName("poster") var poster: Images? = null,
    @SerializedName("type") var type: String? = null,
    @SerializedName("year") var year: Int? = null,
    @SerializedName("rating") var rating: String? = null,
    @SerializedName("slug") var slug: String? = null,
    @SerializedName("watch_uri") private var watchUri: String? = null
) {
    fun getEpisodeSlug() = slug?.split("/")?.getOrNull(2) ?: ""
}

@Keep
data class Images(
    @SerializedName("sm") var sm: String?,
    @SerializedName("hq") var hq: String?
)

open class BaseApiResponse<T>(
    @SerializedName("hadNext") var hasNext: Boolean = false,
    var result: ArrayList<T> = arrayListOf()
)