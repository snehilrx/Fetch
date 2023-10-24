package com.otaku.kickassanime.db.models

import com.google.gson.annotations.SerializedName

enum class TimestampType(val type: String) {
    INTRO("Intro"),
    RECAP("Recap"),
    CANON("Canon"),
    MUST_WATCH("Must Watch"),
    BRANDING("Branding"),
    MIXED_INTRO("Mixed Intro"),
    NEW_INTRO("New Intro"),
    FILLER("Filler"),
    TRANSITION("Transition"),
    CREDITS("Credits"),
    MIXED_CREDITS("Mixed Credits"),
    NEW_CREDITS("New Credits"),
    PREVIEW("Preview"),
    TITLE_CARD("Title Card"),
    UNKNOWN("Unknown")
}

data class Type(
    val name: String
)

data class Timestamps(
    val type: Type,
    @SerializedName("at") var at: Float? = null
)

data class Episodes(
    val number: String?,
    @SerializedName("timestamps") var timestamps: ArrayList<Timestamps> = arrayListOf()
)

data class Data(
    @SerializedName("findEpisodesByShowId") var episodes: ArrayList<Episodes> = arrayListOf()
)

data class Timeline(
    @SerializedName("data") var data: Data? = Data()
)