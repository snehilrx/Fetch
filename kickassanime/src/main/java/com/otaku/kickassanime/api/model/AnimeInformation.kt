package com.otaku.kickassanime.api.model

import com.google.gson.*
import com.google.gson.annotations.SerializedName
import java.lang.reflect.Type


data class AnimeInformation(
    @SerializedName("anime_id") var animeId: String? = null,
    @SerializedName("name") var name: String? = null,
    @SerializedName("en_title") var enTitle: String? = null,
    @SerializedName("slug") var slug: String? = null,
    @SerializedName("description") var description: String? = null,
    @SerializedName("status") var status: String? = null,
    @SerializedName("image") var image: String? = null,
    @SerializedName("banner") var banner: String? = null,
    @SerializedName("startdate") var startdate: String? = null,
    @SerializedName("enddate") var enddate: String? = null,
    @SerializedName("broadcast_day") var broadcastDay: String? = null,
    @SerializedName("broadcast_time") var broadcastTime: String? = null,
    @SerializedName("source") var source: String? = null,
    @SerializedName("duration") var duration: String? = null,
    @SerializedName("alternate") var alternate: String? = null,
    @SerializedName("site") var site: String? = null,
    @SerializedName("info_link") var infoLink: String? = null,
    @SerializedName("createddate") var createddate: String? = null,
    @SerializedName("mal_id") var malId: String? = null,
    @SerializedName("simkl_id") var simklId: String? = null,
    @SerializedName("types") var types: ArrayList<Types>? = arrayListOf(),
    @SerializedName("genres") var genres: ArrayList<Genres>? = arrayListOf(),
    @SerializedName("slug_id") var slugId: String? = null,
    @SerializedName("type") var type: String? = null,
    @SerializedName("episodes") var episodes: ArrayList<Episodes> = arrayListOf(),
    @SerializedName("aid") var aid: String? = null,
    @SerializedName("favorited") var favorited: Boolean? = null,
    @SerializedName("votes") var votes: Int? = null,
    @SerializedName("rating") var rating: Boolean? = null
) {
    companion object {
        @JvmStatic
        val animeInformationDeSerializer =
            JsonDeserializer<AnimeInformation?> { json, typeOfT, context ->
                val jsonObject = json.asJsonObject
                val property = "alternate"
                if (jsonObject.has(property)) {
                    val elem = jsonObject[property]
                    if (elem.isJsonArray) {
                        jsonObject?.addProperty(property, elem.toString())
                    }
                }
                Gson().fromJson(jsonObject, typeOfT)
            }
    }
}