package com.otaku.kickassanime.api.model

import androidx.media3.common.MimeTypes
import com.google.gson.annotations.SerializedName
import java.util.*

data class Maverickki(
    @SerializedName("videoId") var videoId: String? = null,
    @SerializedName("name") var name: String? = null,
    @SerializedName("thumbnail") var thumbnail: String? = null,
    @SerializedName("timelineThumbnail") var timelineThumbnail: String? = null,
    @SerializedName("hls") var hls: String? = null,
    @SerializedName("renditionInProgress") var renditionInProgress: Boolean? = null,
    @SerializedName("subtitles") var subtitles: ArrayList<Subtitles> = arrayListOf()
) {
    companion object {
        internal const val BASE_URL = "https://maverickki.com"
    }

    @Suppress("unused")
    fun link(): String? {
        return if (hls != null) BASE_URL + hls else null
    }
}

data class Subtitles(
    @SerializedName("name") var name: String? = null,
    @SerializedName("src") var src: String? = null
) : CommonSubtitle {
    override fun getLanguage(): String {
        return when (name) {
            "Traditional Chinese",
            "Simplified Chinese" -> "chinese"

            "Indonesian" -> "in"
            "Vietnamese" -> "vi"
            else -> name ?: ""
        }
    }

    override fun getLink(): String {
        return if (src != null) Maverickki.BASE_URL + src else ""
    }

    override fun getFormat(): String = MimeTypes.TEXT_VTT
}