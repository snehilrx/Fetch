package com.otaku.fetch.base.ui

import android.os.Bundle
import android.util.Log
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.AppModuleProvider
import com.otaku.fetch.base.R
import com.otaku.fetch.base.TAG
import com.otaku.fetch.base.databinding.ComposeBinding
import com.otaku.fetch.base.download.DownloadScreen
import com.otaku.fetch.base.download.DownloadUtils
import com.otaku.fetch.base.download.DownloadViewModel
import com.otaku.fetch.base.settings.Settings
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class ComposeFragment : BindingFragment<ComposeBinding>(R.layout.compose) {

    private val downloadsVM: DownloadViewModel by activityViewModels()

    object BaseRoutes {
        const val SETTINGS = "settings"
        const val DOWNLOADS = "downloads"
    }

    @Inject
    lateinit var downloadUtils: DownloadUtils

    override fun onBind(binding: ComposeBinding, savedInstanceState: Bundle?) {
        super.onBind(binding, savedInstanceState)
        val destination = arguments?.getString("destination")
        binding.compose.apply {
            setPadding(paddingLeft, paddingTop + getStatusBarHeight(), paddingRight, paddingBottom)
        }
        binding.compose.setContent {
            if (destination != null) {
                (activity?.application as? AppModuleProvider)?.currentModule?.ComposeTheme {
                    BaseNavHost(
                        startDestination = destination,
                        modifier = Modifier.statusBarsPadding()
                    )
                }
            }
        }
    }

    @Composable
    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    fun BaseNavHost(
        modifier: Modifier = Modifier,
        navController: NavHostController = rememberNavController(),
        startDestination: String
    ) {
        NavHost(
            modifier = modifier,
            navController = navController,
            startDestination = startDestination
        ) {
            composable(BaseRoutes.DOWNLOADS) {
                DownloadScreen(downloadsVM)
            }
            composable(BaseRoutes.SETTINGS) {
                downloadsVM.detachListener()
                Settings()
            }
        }
    }

    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    override fun onDestroy() {
        super.onDestroy()
        downloadsVM.detachListener()
    }

    private fun getStatusBarHeight(): Int {
        var result = 0
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            try {
                result = resources.getDimensionPixelSize(resourceId)
            } catch (e: Exception) {
                Log.e(
                    TAG,
                    "getStatusBarHeight: unable to calculate statusbar size, resources must be an issue",
                    e
                )
            }
        }
        return result
    }
}