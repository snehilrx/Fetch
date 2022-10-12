package com.otaku.fetch

import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import javax.inject.Named


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    @Named("kissanime")
    lateinit var kissanimeModule: AppModule

    private lateinit var modulesList: List<AppModule>

    private lateinit var currentModule: AppModule


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        modulesList = listOf(
            kissanimeModule
        )
        currentModule = kissanimeModule
        setTransparentStatusBar()
        openModule()
    }

    private fun openModule() {
        supportFragmentManager.beginTransaction().replace(
            R.id.host,
            currentModule.getMainFragment()
        ).commit()
    }


    override fun onBackPressed() {
        if (!findNavController(R.id.host).popBackStack()) {
            super.onBackPressed()
        }
    }


    private fun setTransparentStatusBar() {
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        window.statusBarColor = Color.TRANSPARENT
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return NavigationUI.onNavDestinationSelected(
            item,
            NavHostFragment.findNavController(currentModule.getMainFragment())
        )
                || super.onOptionsItemSelected(item)
    }

}