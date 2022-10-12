package com.otaku.kickassanime.api.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class AnimeAndEpisodeInformation (
    @SerializedName("anime") val anime: AnimeInformation? = AnimeInformation(),
    @SerializedName("episode") val episodeInformation: EpisodeInformation? = EpisodeInformation(),
    @SerializedName("ext_servers") val extServers: ArrayList<String> = arrayListOf(),
    @SerializedName("episodes") val episodes: ArrayList<Episodes> = arrayListOf()
)


