package com.otaku.kickassanime.utils

import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter

object Utils {

    @JvmStatic
    val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    fun parseDateTime(dateTime: String): LocalDateTime = LocalDateTime.parse(dateTime, formatter)
}