package com.otaku.kickassanime

import android.content.Context
import androidx.navigation.fragment.NavHostFragment
import com.otaku.fetch.AppModule
import com.otaku.kickassanime.page.MainFragment

class PackageModule : AppModule {

    private val mainFragment: MainFragment = MainFragment()

    override val name: String
        get() = "Kickass Anime"

    override fun onSearch(query: String) {
        TODO("Not yet implemented")
    }

    override fun getMainFragment(link: String): NavHostFragment {
        return mainFragment
    }

    override fun initialize(context: Context){
    }
}