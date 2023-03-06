package com.otaku.fetch

import android.content.Context

interface AppModule {
    /**
     *  Unique identifier
     * */
    val name: String

    fun onSearch(query: String)

    fun getNavigationGraph(): Int

    suspend fun triggerNotification(context: Context)

    fun initialize(query: String?, link: String = "")
}
