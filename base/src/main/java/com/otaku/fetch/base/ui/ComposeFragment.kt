package com.otaku.fetch.base.ui

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.otaku.fetch.AppModuleProvider
import com.otaku.fetch.base.R
import com.otaku.fetch.base.databinding.ComposeBinding
import com.otaku.fetch.base.download.DownloadScreen
import com.otaku.fetch.base.download.DownloadViewModel
import com.otaku.fetch.base.settings.Settings
import com.otaku.fetch.base.settings.dataStore
import com.otaku.fetch.base.utils.UiUtils.PREF_STATUS_BAR_HEIGHT
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class ComposeFragment : BindingFragment<ComposeBinding>(R.layout.compose) {

    private val downloadsVM: DownloadViewModel by viewModels()

    object BaseRoutes {
        const val SETTINGS = "settings"
        const val DOWNLOADS = "downloads"
    }

    override fun onBind(binding: ComposeBinding, savedInstanceState: Bundle?) {
        super.onBind(binding, savedInstanceState)
        val destination = arguments?.getString("destination")
        binding.compose.setContent {
            val dataStore = LocalContext.current.dataStore
            val pref by dataStore.data.collectAsStateWithLifecycle(initialValue = null)

            if (destination != null) {
                (activity?.application as? AppModuleProvider)?.currentModule?.ComposeTheme {
                    BaseNavHost(
                        startDestination = destination,
                        statusBarHeight = pref?.get(PREF_STATUS_BAR_HEIGHT)?.toFloat() ?: 0f,
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
                    statusBarHeight
                ) { shinebar -> setupShineBar(shinebar) }
            }
            composable(BaseRoutes.SETTINGS) {
                Settings(
                    statusBarHeight,
                ) { shinebar -> setupShineBar(shinebar) }
            }
        }
    }

    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    override fun onDestroy() {
        downloadsVM.onCleared()
        super.onDestroy()
    }

}