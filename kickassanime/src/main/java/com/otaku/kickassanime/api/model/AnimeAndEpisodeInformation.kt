package com.otaku.kickassanime.api.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class AnimeAndEpisodeInformation(
    @SerializedName("anime_id"          ) var animeId           : String?            = null,
    @SerializedName("name"              ) var name              : String?            = null,
    @SerializedName("title"             ) var title             : String?            = null,
    @SerializedName("description"       ) var description       : String?            = null,
    @SerializedName("seasonId"          ) var seasonId          : String?            = null,
    @SerializedName("seasonNumber"      ) var seasonNumber      : Int?               = null,
    @SerializedName("episodeNumber"     ) var episodeNumber     : Int?               = null,
    @SerializedName("thumbnail"         ) var thumbnail         : Images?            = null,
    @SerializedName("lang"              ) var lang              : String?            = null,
    @SerializedName("genres"            ) var genres            : ArrayList<String>  = arrayListOf(),
    @SerializedName("year"              ) var year              : Int?               = null,
    @SerializedName("maturityRatings"   ) var maturityRatings   : ArrayList<String>  = arrayListOf(),
    @SerializedName("isDubbed"          ) var isDubbed          : Boolean?           = null,
    @SerializedName("isSubbed"          ) var isSubbed          : Boolean?           = null,
    @SerializedName("isMature"          ) var isMature          : Boolean?           = null,
    @SerializedName("duration"          ) var duration          : String?            = null,
    @SerializedName("slug"              ) var slug              : String?            = null,
    @SerializedName("premiered"         ) var premiered         : String?            = null,
    @SerializedName("episodeNavigation" ) var episodeNavigation : EpisodeNavigation? = EpisodeNavigation(),
    @SerializedName("servers"           ) var servers           : ArrayList<String>  = arrayListOf()
)

data class EpisodeNavigation (
    @SerializedName("prev" ) var prev : String? = null,
    @SerializedName("next" ) var next : String? = null
)

