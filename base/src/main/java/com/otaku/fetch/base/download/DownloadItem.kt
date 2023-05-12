package com.otaku.fetch.base.download

import android.os.Bundle

class DownloadItem(
    val animeTitle: String,
    val episodeNumber: Float,
    val episodeKey: String,
    val animeKey: String,
    val launchBundle: Bundle,
    val launchActivity: Class<*>
)