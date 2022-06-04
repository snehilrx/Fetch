package com.otaku.fetch

import android.content.res.Resources
import android.graphics.drawable.Drawable
import androidx.fragment.app.Fragment

interface AppModule {
    val name: String

    fun icon(resources: Resources): Drawable?

    fun onSearch(query: String)

    fun getMainFragment(link: String = ""): Fragment
}