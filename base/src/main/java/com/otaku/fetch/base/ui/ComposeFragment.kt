package com.otaku.fetch.base.ui

import android.os.Bundle
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
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
import com.otaku.fetch.base.databinding.ComposeBinding
import com.otaku.fetch.base.download.DownloadScreen
import com.otaku.fetch.base.download.DownloadUtils
import com.otaku.fetch.base.download.DownloadViewModel
import com.otaku.fetch.base.settings.Settings
import com.otaku.fetch.base.utils.UiUtils.statusBarHeight
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
        binding.compose.setContent {
            if (destination != null) {
                (activity?.application as? AppModuleProvider)?.currentModule?.ComposeTheme {
                    Box(modifier = Modifier.fillMaxSize()) {
                        BaseNavHost(
                            modifier = Modifier.statusBarsPadding(),
                            startDestination = destination,
                            statusBarHeight = activity?.statusBarHeight?.toFloat(),
                        )
                    }
                }
            }
        }
    }

    @Composable
    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    fun BaseNavHost(
        modifier: Modifier = Modifier,
        navController: NavHostController = rememberNavController(),
        startDestination: String,
        statusBarHeight: Float? = null,
    ) {
        NavHost(
            modifier = modifier,
            navController = navController,
            startDestination = startDestination
        ) {
            composable(BaseRoutes.DOWNLOADS) {
                DownloadScreen(
                    downloadsVM,
                    statusBarHeight,

                    ) { shinebar -> setupShineBar(shinebar) }
            }
            composable(BaseRoutes.SETTINGS) {
                downloadsVM.detachListener()
                Settings(
                    statusBarHeight,
                ) { shinebar -> setupShineBar(shinebar) }
            }
        }
    }

    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    override fun onDestroy() {
        super.onDestroy()
        downloadsVM.detachListener()
    }

}