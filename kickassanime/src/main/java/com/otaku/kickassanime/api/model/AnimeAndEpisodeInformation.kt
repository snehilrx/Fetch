package com.otaku.kickassanime.api.model

import com.google.gson.annotations.SerializedName

data class AnimeAndEpisodeInformation (
    @SerializedName("anime"       ) var anime      : AnimeInformation?   = AnimeInformation(),
    @SerializedName("episode"     ) var episode    : Episode?            = Episode(),
    @SerializedName("ext_servers" ) var extServers : ArrayList<String>   = arrayListOf(),
    @SerializedName("episodes"    ) var episodes   : ArrayList<Episodes> = arrayListOf()
)


