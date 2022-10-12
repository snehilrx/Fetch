package com.otaku.kickassanime.page.frontpage

import androidx.fragment.app.Fragment
import androidx.navigation.NavDirections
import androidx.navigation.fragment.NavHostFragment
import com.otaku.fetch.AppbarController
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
open class FrontPageBaseFragment : Fragment() {

    protected fun navigate(directions: NavDirections) {
        (parentFragment as NavHostFragment).navController.navigate(directions)
    }

    val appbarController by lazy { activity as AppbarController }
}


