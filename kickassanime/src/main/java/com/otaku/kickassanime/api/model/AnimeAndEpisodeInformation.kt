package com.otaku.kickassanime.api.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class AnimeAndEpisodeInformation(
    @SerializedName("anime") val anime: AnimeInformation? = AnimeInformation(),
    @SerializedName("episode") val episodeInformation: EpisodeInformation? = EpisodeInformation(),
    // json object @SerializedName("ext_servers") val extServers: List<String> = arrayListOf(),
    @SerializedName("episodes") val episodes: List<Episodes> = arrayListOf()
)


