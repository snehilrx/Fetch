package com.otaku.kickassanime.api.model

import com.google.gson.annotations.SerializedName

data class Episode (
    @SerializedName("name"        ) var name        : String?  = null,
    @SerializedName("title"       ) var title       : String?  = null,
    @SerializedName("slug"        ) var slug        : String?  = null,
    @SerializedName("slug_id"     ) var slugId      : String?  = null,
    @SerializedName("dub"         ) var dub         : String?  = null,
    @SerializedName("link1"       ) var link1       : String?  = null,
    @SerializedName("link2"       ) var link2       : String?  = null,
    @SerializedName("link3"       ) var link3       : String?  = null,
    @SerializedName("link4"       ) var link4       : String?  = null,
    @SerializedName("anime_id"    ) var animeId     : String?  = null,
    @SerializedName("sector"      ) var sector      : String?  = null,
    @SerializedName("createddate" ) var createddate : String?  = null,
    @SerializedName("next"        ) var next        : Prev?  = null,
    @SerializedName("prev"        ) var prev        : Prev?    = null,
    @SerializedName("epid"        ) var epid        : String?  = null,
    @SerializedName("rating"      ) var rating      : Int?     = null,
    @SerializedName("votes"       ) var votes       : String?  = null,
    @SerializedName("favorited"   ) var favorited   : Boolean? = null
)