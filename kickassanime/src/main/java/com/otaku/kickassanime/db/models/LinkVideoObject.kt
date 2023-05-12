package com.otaku.kickassanime.db.models

import android.net.Uri

class LinkVideoObject(
    private val url: String,
    private val mediaType: Int
) : CommonVideoLink {
    override fun getLink(): String {
        return url
    }

    override fun getLinkName(): String {
        return try {
            Uri.parse(url).host ?: "Kick Server"
        } catch (e: NullPointerException) {
            "Kick Server"
        }
    }

    override fun getVideoType(): Int {
        return mediaType
    }

}