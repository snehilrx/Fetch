package com.otaku.fetch.base.ui

import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.lapism.search.widget.MaterialSearchView

interface SearchInterface {
    fun openSearch()
    fun closeSearch()
    fun setSuggestions(adapter: RecyclerView.Adapter<*>)
    fun showError(e: Throwable)
    fun showLoading()
    fun showContent()
    fun setQueryListener(listener: MaterialSearchView.OnQueryTextListener)
}

val Fragment.searchInterface: SearchInterface?
    get() = (activity as? SearchInterface)