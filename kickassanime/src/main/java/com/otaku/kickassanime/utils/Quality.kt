package com.otaku.kickassanime.utils

enum class Quality(val bitrate: Int) {
    MAX(Constants.QualityBitRate.MAX),
    P_1080(Constants.QualityBitRate.P_1080),
    P_720(Constants.QualityBitRate.P_720),
    P_480(Constants.QualityBitRate.P_480),
    P_360(Constants.QualityBitRate.P_360);
}