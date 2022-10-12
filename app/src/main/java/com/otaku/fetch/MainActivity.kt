package com.otaku.fetch

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.addCallback
import androidx.core.view.isVisible
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lapism.search.widget.MaterialSearchView
import com.lapism.search.widget.NavigationIconCompat
import com.otaku.fetch.base.ui.BindingActivity
import com.otaku.fetch.base.ui.SearchInterface
import com.otaku.fetch.databinding.ActivityMainBinding
import com.otaku.kickassanime.page.frontpage.FrontPageFragmentDirections
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import javax.inject.Named


@AndroidEntryPoint
class MainActivity : BindingActivity<ActivityMainBinding>(R.layout.activity_main), SearchInterface {

    inner class SearchComponents(
        val suggestionsList: RecyclerView,
        val errorLabel: TextView,
        val progressBar: ProgressBar
    )

    private lateinit var searchUi: SearchComponents

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
        initSearchView(binding)
    }

    private fun initSearchView(binding: ActivityMainBinding) {
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
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        val searchSuggestionsList = RecyclerView(searchView.context).apply { layoutParams = params }
        searchSuggestionsList.layoutManager = LinearLayoutManager(searchView.context)

        val label = LayoutInflater.from(searchView.context)
            .inflate(
                com.google.android.material.R.layout.m3_auto_complete_simple_item,
                searchView,
                false
            ) as TextView

        val progressBar = ProgressBar(searchView.context).apply { layoutParams = params }

        label.isVisible = false
        progressBar.isVisible = false

        searchUi = SearchComponents(searchSuggestionsList, label, progressBar)

        searchView.apply {
            findViewById<View>(com.lapism.search.R.id.search_view_background)
                ?.setPaddingRelative(0, _statusBarHeight, 0, 0)
            addView(searchSuggestionsList)
            addView(label)
            addView(progressBar)
            navigationIconCompat = NavigationIconCompat.ARROW
            setNavigationOnClickListener {
                clearFocus()
            }
            setHint(getString(com.otaku.kickassanime.R.string.search_anime))
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

    override fun setQueryListener(listener: MaterialSearchView.OnQueryTextListener) {
        binding.searchView.setOnQueryTextListener(listener)
    }

}