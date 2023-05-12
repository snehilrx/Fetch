package com.otaku.kickassanime.page.home

import android.os.Bundle
import androidx.navigation.fragment.NavHostFragment
import com.otaku.kickassanime.R

class HomeFragment : NavHostFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        navController.setGraph(R.navigation.navigation_home)
    }

}