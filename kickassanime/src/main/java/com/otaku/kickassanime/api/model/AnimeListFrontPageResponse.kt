package com.otaku.kickassanime.api.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class Anime(
    @SerializedName("isSimulcast"   ) var isSimulcast   : Boolean?,
    @SerializedName("isSubbed"      ) var isSubbed      : Boolean?,
    @SerializedName("isDubbed"      ) var isDubbed      : Boolean?,
    @SerializedName("year"          ) var year          : Int?,
    @SerializedName("slug"          ) var slug          : String? ,
    @SerializedName("episodeNumber" ) var episodeNumber : Int?,
    @SerializedName("title"         ) var title         : String?,
    @SerializedName("poster"        ) var poster        : Poster?,
    @SerializedName("lastUpdate"    ) var lastUpdate    : String?,
    @SerializedName("updatedString" ) var updatedString : String? 
)

data class Image (
    @SerializedName("name"    ) var name    : String?,
    @SerializedName("formats" ) var formats : ArrayList<String> = arrayListOf(),
    @SerializedName("width"   ) var width   : Int?,
    @SerializedName("height"  ) var height  : Int?
)

@Keep
data class Poster (
    @SerializedName("sm" ) var sm : Image?,
    @SerializedName("hq" ) var hq : Image?
)