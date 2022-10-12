package com.otaku.fetch

import androidx.fragment.app.Fragment

interface AppModule {
    val name: String

    fun onSearch(query: String)

    fun getMainFragment(link: String = ""): Fragment
}