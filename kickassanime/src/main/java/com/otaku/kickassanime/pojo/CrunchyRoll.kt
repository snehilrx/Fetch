package com.otaku.kickassanime.pojo

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.otaku.kickassanime.db.models.CommonVideoLink

@Keep
data class CrunchyRoll(
    @SerializedName("sources") var streams: ArrayList<Streams> = arrayListOf()
)

@Keep
data class Streams(
    @SerializedName("type") var format: String? = null,
    @SerializedName("file") var url: String? = null
) : CommonVideoLink {
    override fun getLink(): String {
        return "https:$url"
    }

    override fun getLinkName(): String {
        return "Crunchy"
    }

    override fun getVideoType(): Int {
        return when (format) {
            "hls" -> CommonVideoLink.HLS
            "dash" -> CommonVideoLink.DASH
            else -> CommonVideoLink.HLS
        }
    }

}