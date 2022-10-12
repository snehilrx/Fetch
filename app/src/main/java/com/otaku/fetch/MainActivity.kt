package com.otaku.fetch

import android.app.ActionBar
import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.otaku.fetch.base.ui.BindingActivity
import com.otaku.fetch.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import javax.inject.Named


@AndroidEntryPoint
class MainActivity : BindingActivity<ActivityMainBinding>(R.layout.activity_main) {

    @Inject
    @Named("kickassanime")
    lateinit var kissanimeModule: AppModule

    private lateinit var modulesList: List<AppModule>

    private lateinit var currentModule: AppModule

    override fun onBind(binding: ActivityMainBinding, savedInstanceState: Bundle?) {
        super.onBind(binding, savedInstanceState)
        modulesList = listOf(
            kissanimeModule
        )
        currentModule = kissanimeModule
        setTransparentStatusBar()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return NavigationUI.onNavDestinationSelected(
            item,
            NavHostFragment.findNavController(currentModule.getMainFragment())
        ) || super.onOptionsItemSelected(item)
    }



}