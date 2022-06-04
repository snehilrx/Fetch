package com.otaku.fetch

import android.graphics.Color
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.commit
import com.otaku.fetch.base.ui.ShineView
import com.otaku.fetch.base.ui.UiUtils
import com.otaku.kickassanime.PackageModule
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private var shineBar: ShineView? = null
    private var toolbar: Toolbar? = null

    private var _statusBarHeight: Int = 0

    @Inject
    lateinit var kissanimeModule: PackageModule

    private lateinit var modulesList: List<AppModule>

    private lateinit var currentModule: AppModule


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        modulesList = listOf(
            kissanimeModule
        )
        currentModule = kissanimeModule
        _statusBarHeight = getStatusBarHeight()
        setContentView(R.layout.activity_main)
        setTransparentStatusBar()
        toolbar = findViewById(R.id.toolbar)
        shineBar = findViewById(R.id.shineView)
        setSupportActionBar(toolbar)
        setupToolbar()
        initializeShineBar()
        initializeModule()
    }

    private fun initializeShineBar() {
        shineBar?.appBarIdRes = R.id.appbar
        shineBar?.statusbarHeight = _statusBarHeight.toFloat()
    }

    private fun initializeModule() {
        initializeAppModuleIcon()
        supportFragmentManager.commit {
            replace(R.id.host, currentModule.getMainFragment())
        }
    }

    private fun setupToolbar() {
        toolbar?.layoutParams = toolbar?.layoutParams?.apply {
            height += _statusBarHeight
        }
        toolbar?.apply {
            setPadding(paddingLeft, paddingTop + _statusBarHeight, paddingRight, paddingBottom)
        }
        toolbar?.elevation = 0f
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
            shineBar?.shineColor = it
        }
    }
}