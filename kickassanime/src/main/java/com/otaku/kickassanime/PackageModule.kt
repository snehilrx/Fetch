package com.otaku.kickassanime

import android.content.res.Resources
import android.graphics.drawable.Drawable
import androidx.core.content.res.ResourcesCompat
import androidx.navigation.fragment.NavHostFragment
import com.otaku.fetch.AppModule
import com.otaku.kickassanime.page.MainFragment

class PackageModule : AppModule {

    private val mainFragment: MainFragment = MainFragment()

    override val name: String
        get() = "Kickass Anime"

    override fun icon(resources: Resources): Drawable? {
        return ResourcesCompat.getDrawable(resources, R.drawable.logo, mainFragment.activity?.theme)
    }

    override fun onSearch(query: String) {
        TODO("Not yet implemented")
    }

    override fun getMainFragment(link: String): NavHostFragment {
        return mainFragment
    }
}