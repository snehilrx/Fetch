package com.otaku.fetch

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.addCallback
import androidx.core.view.isVisible
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lapism.search.widget.MaterialSearchView
import com.lapism.search.widget.NavigationIconCompat
import com.otaku.fetch.base.askNotificationPermission
import com.otaku.fetch.base.settings.Settings
import com.otaku.fetch.base.settings.dataStore
import com.otaku.fetch.base.ui.BindingActivity
import com.otaku.fetch.base.ui.SearchInterface
import com.otaku.fetch.base.utils.UiUtils.statusBarHeight
import com.otaku.fetch.databinding.ActivityModuleBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


@AndroidEntryPoint
class ModuleActivity :
    BindingActivity<ActivityModuleBinding>(R.layout.activity_module), SearchInterface {

    inner class SearchComponents(
        val suggestionsList: RecyclerView,
        val errorLabel: TextView,
        val progressBar: ProgressBar
    )

    private lateinit var searchUi: SearchComponents

    override fun onBind(binding: ActivityModuleBinding, savedInstanceState: Bundle?) {
        super.onBind(binding, savedInstanceState)
        initNavigationView()
        setTransparentStatusBar()
        initSearchView(binding)
        checkPermissions()
    }

    private fun checkPermissions() {
        lifecycleScope.launch {
            dataStore.data.collectLatest {
                if (it[Settings.PREF_DEFAULTS_SET] != true) {
                    dataStore.edit { pref ->
                        pref[Settings.PREF_DEFAULTS_SET] = true
                        pref[Settings.SKIP_ENABLED] = true
                        pref[Settings.NOTIFICATION_ENABLED] = askNotificationPermission()
                        pref[Settings.AUTO_RESUME] = true
                        pref[Settings.STREAM_VIDEO_QUALITY] =
                            resources.getStringArray(com.otaku.fetch.base.R.array.video_qualities)[0]
                        pref[Settings.DOWNLOADS_VIDEO_QUALITY] =
                            resources.getStringArray(com.otaku.fetch.base.R.array.video_qualities)[0]
                    }
                }
            }
        }
    }

    private fun initNavigationView() {
        var appModule = (application as? FetchApplication)?.currentModule
        val deepLink = intent.extras?.getString(ARG_MODULE_DEEPLINK)
        if (appModule == null) {
            val moduleName = intent.extras?.getString(ARG_MODULE_NAME)
            if (moduleName == null) {
                finish()
                return
            } else {
                appModule = ModuleRegistry.getModulesList().find {
                    it.displayName.equals(moduleName, true)
                }?.appModule
                (application as? FetchApplication)?.currentModule = appModule

            }
        }
        if (appModule == null) {
            finish()
            return
        }
        val navHostFragment = binding.fragmentContainerView.getFragment<NavHostFragment>()
        navHostFragment.navController.setGraph(appModule.getNavigationGraph())
        deepLink?.let { navHostFragment.navController.navigate(it) }
        binding.bottomNavigation?.inflateMenu(appModule.getBottomNavigationMenu())
        binding.bottomNavigation?.setupWithNavController(navHostFragment.navController)
        binding.railNavigation?.inflateMenu(appModule.getBottomNavigationMenu())
        binding.railNavigation?.setupWithNavController(navHostFragment.navController)
    }

    @SuppressLint("PrivateResource", "ClickableViewAccessibility")
    private fun initSearchView(binding: ActivityModuleBinding) {
        val searchView = binding.searchView
        val callback = this.onBackPressedDispatcher.addCallback {
            searchView.clearFocus()
        }
        searchView.setOnFocusChangeListener(object :
            MaterialSearchView.OnFocusChangeListener {
            override fun onFocusChange(hasFocus: Boolean) {
                callback.isEnabled = hasFocus
            }
        })
        val params = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        val searchSuggestionsList = RecyclerView(searchView.context).apply { layoutParams = params }
        searchSuggestionsList.layoutManager = LinearLayoutManager(searchView.context)
        searchSuggestionsList.setOnTouchListener { view, _ ->
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.hideSoftInputFromWindow(view.windowToken, 0) ?: false
        }
        val label = LayoutInflater.from(searchView.context)
            .inflate(
                com.google.android.material.R.layout.m3_auto_complete_simple_item,
                searchView,
                false
            ) as TextView

        val progressBar = ProgressBar(searchView.context).apply {
            layoutParams = params.apply {
                height =
                    ViewGroup.LayoutParams.WRAP_CONTENT
            }
        }

        label.isVisible = false
        progressBar.isVisible = false

        searchUi = SearchComponents(searchSuggestionsList, label, progressBar)

        searchView.apply {
            statusBarHeight {
                runOnUiThread {
                    findViewById<View>(com.lapism.search.R.id.search_view_background)
                        ?.setPadding(0, it, 0, 0)
                }
            }
            addView(searchSuggestionsList)
            addView(label)
            addView(progressBar)
            navigationIconCompat = NavigationIconCompat.ARROW
            setNavigationOnClickListener {
                clearFocus()
            }
            setHint(getString(com.otaku.kickassanime.R.string.search_anime))
        }
        val searchEdit =
            searchView.findViewById<EditText>(com.lapism.search.R.id.search_view_edit_text)
        val oldFocus = searchEdit.onFocusChangeListener
        searchEdit?.setOnFocusChangeListener { v, b ->
            binding.bottomNavigation?.isVisible = !b
            binding.railNavigation?.isVisible = !b
            searchEdit.text.clear()
            oldFocus.onFocusChange(v, b)
        }

    }


    override fun openSearch() {
        binding.searchView.requestFocus()
    }

    override fun closeSearch() {
        binding.searchView.clearFocus()
    }

    override fun setSuggestions(adapter: RecyclerView.Adapter<*>) {
        searchUi.suggestionsList.adapter = adapter
    }

    override fun showError(e: Throwable) {
        searchUi.errorLabel.isVisible = true
        searchUi.progressBar.isVisible = false
        searchUi.suggestionsList.isVisible = false
        searchUi.errorLabel.text = e.message
    }

    override fun showLoading() {
        searchUi.errorLabel.isVisible = false
        searchUi.progressBar.isVisible = true
        searchUi.suggestionsList.isVisible = false
    }

    override fun showContent() {
        searchUi.errorLabel.isVisible = false
        searchUi.progressBar.isVisible = false
        searchUi.suggestionsList.isVisible = true
    }

    override fun setQueryListener(listener: MaterialSearchView.OnQueryTextListener?) {
        binding.searchView.setOnQueryTextListener(listener)
    }
    companion object {
        const val ARG_MODULE_DEEPLINK = "data"
        const val ARG_MODULE_NAME = "name"
    }
}