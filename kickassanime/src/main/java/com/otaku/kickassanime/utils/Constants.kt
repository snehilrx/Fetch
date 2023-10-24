package com.otaku.kickassanime.utils

object Constants {

    const val NETWORK_PAGE_SIZE: Int = 23
    const val cacheTimeoutInHours = 24L
    const val patternDateTime = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"

    @JvmStatic
    val headersMap: Map<String, String> = mapOf("origin" to "https://kaavid.com")

    object QualityBitRate {
        const val MAX = Int.MAX_VALUE
        const val P_1080 = 4034000
        const val P_720 = 1942000
        const val P_480 = 1280000
        const val P_360 = 749000
    }


}