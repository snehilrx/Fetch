package com.otaku.fetch

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.work.Data

interface AppModule {
    val name: String

    fun onSearch(query: String)

    fun getMainFragment(): Fragment

    suspend fun triggerNotification(context: Context)

    fun initialize(query: String?, link: String = "")
}