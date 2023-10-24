package com.otaku.kickassanime.pojo

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class PlayData(
    @SerializedName("sources") var sources: ArrayList<Sources> = arrayListOf(),
    @SerializedName("allSources") var allSources: ArrayList<Sources> = arrayListOf(),
    @SerializedName("chapters") var chapters: Chapters? = Chapters()
)

@Keep
data class Tracks(
    @SerializedName("kind") var kind: String? = null,
    @SerializedName("file") var file: String? = null,
    @SerializedName("label") var label: String? = null,
    @SerializedName("default") var default: Boolean? = null
)

@Keep
data class Sources(

    @SerializedName("default") var default: Boolean? = null,
    @SerializedName("type") var type: String? = null,
    @SerializedName("image") var image: String? = null,
    @SerializedName("file") var file: String? = null,
    @SerializedName("tracks") var tracks: ArrayList<Tracks> = arrayListOf(),
    @SerializedName("label") var label: String? = null,
    @SerializedName("preload") var preload: String? = null

)

@Keep
data class Chapters(
    @SerializedName("defaultLanguage") var defaultLanguage: String? = null,
    @SerializedName("timestamps") var timestamps: ArrayList<String> = arrayListOf()
)