package com.otaku.kickassanime

import android.content.res.Resources
import android.graphics.drawable.Drawable
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import com.otaku.fetch.AppModule
import com.otaku.kickassanime.page.MainFragment
import javax.inject.Inject

class PackageModule @Inject constructor() : AppModule {

    private val mainFragment: MainFragment = MainFragment()

    override val name: String
        get() = "Kickass Anime"

    override fun icon(resources: Resources): Drawable? {
        return ResourcesCompat.getDrawable(resources, R.drawable.logo, mainFragment.activity?.theme)
    }

    override fun onSearch(query: String) {
        TODO("Not yet implemented")
    }

    override fun getMainFragment(link: String): Fragment {
        return mainFragment
    }
}