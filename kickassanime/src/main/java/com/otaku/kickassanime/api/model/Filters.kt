package com.otaku.kickassanime.api.model

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class Filters(
    val genres: List<String>?,
    val types: List<String>?,
    val years: List<String>?,
) : Parcelable