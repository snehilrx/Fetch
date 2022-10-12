package com.otaku.kickassanime.api.model

import com.google.gson.annotations.SerializedName

data class Maverickki (

    @SerializedName("videoId"             ) var videoId             : String?           = null,
    @SerializedName("name"                ) var name                : String?           = null,
    @SerializedName("thumbnail"           ) var thumbnail           : String?           = null,
    @SerializedName("timelineThumbnail"   ) var timelineThumbnail   : String?           = null,
    @SerializedName("hls"                 ) var hls                 : String?           = null,
    @SerializedName("renditionInProgress" ) var renditionInProgress : Boolean?          = null,
    @SerializedName("subtitles"           ) var subtitles           : ArrayList<String> = arrayListOf()
){
    companion object{
        private const val BASE_URL = "https://maverickki.com"
    }

    fun link(): String? {
        return if(hls != null) BASE_URL + hls else null
    }
}