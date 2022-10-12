package com.otaku.fetch

import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.google.android.material.appbar.AppBarLayout
import com.otaku.fetch.base.ui.UiUtils
import com.otaku.fetch.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import javax.inject.Named


@AndroidEntryPoint
class MainActivity : AppCompatActivity(), AppbarController {

    private lateinit var bindings: ActivityMainBinding

    private var _statusBarHeight: Int = 0

    @Inject
    @Named("kissanime")
    lateinit var kissanimeModule: AppModule

    private lateinit var modulesList: List<AppModule>

    private lateinit var currentModule: AppModule

    private var currentFragment = UNKNOWN_FRAGMENT


    override fun onCreate(savedInstanceState: Bundle?) {
        bindings = DataBindingUtil.setContentView(this, R.layout.activity_main)
        super.onCreate(savedInstanceState)
        modulesList = listOf(
            kissanimeModule
        )
        currentModule = kissanimeModule
        _statusBarHeight = getStatusBarHeight()
        restore(savedInstanceState)
        setTransparentStatusBar()
        setSupportActionBar(bindings.toolbar)
        setupToolbar()
        initializeShineView()
        initializeAppModuleIcon()
        openModule()
    }

    private fun restore(savedInstanceState: Bundle?) {
        savedInstanceState?.getInt(BUNDLE_KEY_CURRENT_FRAGMENT)?.let { currentFragment = it }
    }

    private fun initializeShineView() {
        bindings.shineView.appBarIdRes = R.id.appbar
        bindings.shineView.statusbarHeight = _statusBarHeight.toFloat()
    }


    private fun openModule() {
        supportFragmentManager.beginTransaction().replace(
            R.id.host,
            currentModule.getMainFragment()
        ).commit()
    }

    private fun setupToolbar() {
        bindings.toolbar.layoutParams = bindings.toolbar.layoutParams?.apply {
            height += _statusBarHeight
        }
        bindings.episodeNumber.layoutParams =
            (bindings.episodeNumber.layoutParams as? ViewGroup.MarginLayoutParams)?.apply {
                topMargin += _statusBarHeight
                leftMargin += _statusBarHeight
            }
        bindings.toolbar.apply {
            setPadding(paddingLeft, paddingTop + _statusBarHeight, paddingRight, paddingBottom)
        }
        bindings.toolbar.setCollapsible(true)
    }

    override fun onBackPressed() {
        if (!findNavController(R.id.host).popBackStack()) {
            super.onBackPressed()
        }
    }

    private fun getStatusBarHeight(): Int {
        var result = 0
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = resources.getDimensionPixelSize(resourceId)
        }
        return result
    }

    private fun setTransparentStatusBar() {
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        window.statusBarColor = Color.TRANSPARENT
    }

    private fun initializeAppModuleIcon() {
        UiUtils.getColor(currentModule.icon(resources)) {
            bindings.shineView.shineColor = it
        }
    }


    override fun getAppbar() = bindings.appbar

    override fun getToolbar() = bindings.toolbar

    override fun getAppBarImage() = bindings.appbarImageView

    override fun getAppBarEpisodeChip() = bindings.episodeNumber

    override fun getCollapsingToolbar() = bindings.collapsingToolbar

    override fun getFullScreenVideoView() = bindings.fullScreenVideoViewContainer

    override fun saveAppbar() = getAppBarBehavior()?.topAndBottomOffset

    override fun restoreAppbar(offset: Int) {
        getAppBarBehavior()?.topAndBottomOffset = offset
        if(offset < 0) {
            bindings.appbar.setExpanded(false, true)
        } else {
            bindings.appbar.setExpanded(true, true)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return NavigationUI.onNavDestinationSelected(
            item,
            NavHostFragment.findNavController(currentModule.getMainFragment())
        )
                || super.onOptionsItemSelected(item)
    }

    public override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(BUNDLE_KEY_CURRENT_FRAGMENT, currentFragment)
        super.onSaveInstanceState(outState)
    }

    private fun getAppBarBehavior() =
        (bindings.appbar.layoutParams as? CoordinatorLayout.LayoutParams)?.behavior as? AppBarLayout.Behavior

    companion object {
        private const val UNKNOWN_FRAGMENT = 1
        private const val MAIN_MODULE_FRAGMENT = 2
        private const val NO_NETWORK_FRAGMENT = 3

        private const val BUNDLE_KEY_CURRENT_FRAGMENT = "current_fragment"
    }
}