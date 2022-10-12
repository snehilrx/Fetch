package com.otaku.kickassanime.page

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.otaku.fetch.AppbarController
import com.otaku.kickassanime.R

private const val ARG_APP_LINK = "appLink"

/**
 * A simple [Fragment] subclass.
 * Use the [MainFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MainFragment : NavHostFragment() {
    private var link: String? = null

    private val appbarController by lazy { activity as AppbarController }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            link = it.getString(ARG_APP_LINK)
        }
        navController.setGraph(R.navigation.navigation_kickassanime)
        navController.enableOnBackPressed(true)
        NavigationUI.setupWithNavController(appbarController.getCollapsingToolbar(), appbarController.getToolbar(), navController)
    }



    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param kissanimeLink Parameter 1.
         * @return A new instance of fragment MainFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(kissanimeLink: String) =
            MainFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_APP_LINK, kissanimeLink)
                }
            }
    }
}