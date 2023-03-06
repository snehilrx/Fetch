package com.otaku.fetch

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.addCallback
import androidx.core.view.isVisible
import androidx.navigation.createGraph
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lapism.search.widget.MaterialSearchView
import com.lapism.search.widget.NavigationIconCompat
import com.otaku.fetch.base.ui.BindingActivity
import com.otaku.fetch.base.ui.SearchInterface
import com.otaku.fetch.databinding.ActivityModuleBinding
import dagger.hilt.android.AndroidEntryPoint


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
    }

    private fun initNavigationView() {
        val graph = intent.extras?.getInt(ARG_MODULE_GRAPH) ?: return
        val navHostFragment = binding.fragmentContainerView.getFragment<NavHostFragment>()
        navHostFragment.navController.setGraph(graph)
    }

    @SuppressLint("PrivateResource")
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
                ?.setPaddingRelative(0, mStatusBarHeight, 0, 0)
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
            searchView.findViewById<View>(com.lapism.search.R.id.search_view_edit_text)
        val oldFocus = searchEdit.onFocusChangeListener
        searchEdit?.setOnFocusChangeListener{ v, b ->
            binding.fragmentContainerView.findViewById<View>(com.otaku.kickassanime.R.id.front)?.isVisible = !b
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

    override fun setQueryListener(listener: MaterialSearchView.OnQueryTextListener) {
        binding.searchView.setOnQueryTextListener(listener)
    }

    companion object {
        const val ARG_MODULE_GRAPH = "module"
    }
}