package com.otaku.kickassanime.api.model

import android.net.Uri
import androidx.media3.common.MimeTypes
import com.google.gson.annotations.SerializedName
import com.otaku.kickassanime.db.models.CommonVideoLink


data class AddKaa (

    @SerializedName("subtitles" ) var subtitles : ArrayList<SubtitlesAdd> = arrayListOf(),
    @SerializedName("streams"   ) var streams   : ArrayList<Streams>   = arrayListOf(),
    @SerializedName("thumbnail" ) var thumbnail : KThumbnail?           = KThumbnail()

)
@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
data class SubtitlesAdd (
    @SerializedName("language" ) var lang : String? = null,
    @SerializedName("url"      ) var url      : String? = null
) : CommonSubtitle {
    override fun getLink(): String {
        return url ?: ""
    }

    override fun getLanguage(): String {
        return lang ?: ""
    }

    override fun getFormat(): String {
        return MimeTypes.TEXT_SSA
    }

}

data class KThumbnail (

    @SerializedName("width"  ) var width  : Int?    = null,
    @SerializedName("height" ) var height : Int?    = null,
    @SerializedName("url"    ) var url    : String? = null

)

data class Streams (

    @SerializedName("format"       ) var format      : String? = null,
    @SerializedName("audio_lang"   ) var audioLang   : String? = null,
    @SerializedName("hardsub_lang" ) var hardsubLang : String? = null,
    @SerializedName("url"          ) var url         : String? = null

) : CommonVideoLink {
    override fun getLink(): String {
        return url ?: ""
    }

    override fun getLinkName(): String {
        return try {
            Uri.parse(url).host ?: "Kick Server"
        } catch (e: NullPointerException) {
            "Kick Server"
        }
    }

    override fun getVideoType(): Int {
        return if (format == "adaptive_dash")
            CommonVideoLink.DASH
        else
            CommonVideoLink.HLS
    }

}