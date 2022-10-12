package com.otaku.fetch

import android.content.Context
import androidx.fragment.app.Fragment

interface AppModule {
    val name: String

    fun onSearch(query: String)

    fun getMainFragment(link: String = ""): Fragment

    fun initialize(context: Context)
}