package com.otaku.kickassanime.page.frontpage.data

import android.text.TextUtils
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.otaku.fetch.base.TAG
import com.otaku.fetch.base.livedata.State
import com.otaku.fetch.base.utils.UiUtils.throttleLatest
import com.otaku.kickassanime.db.models.AnimeSearchResult
import com.otaku.kickassanime.page.search.SearchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.zip
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class FrontPageViewModel @Inject constructor(
    private val frontPageRepository: FrontPageRepository,
    private val searchRepository: SearchRepository
) : ViewModel() {
    private fun recentLimited() = frontPageRepository.getFirst30RecentItems()
    private fun recentSubLimited() = frontPageRepository.getFirst30RecentSubItems()
    private fun recentDubLimited() = frontPageRepository.getFirst30RecentDubItems()

    fun zipped() = recentLimited().zip(recentSubLimited()) { x, y -> Pair(x, y) }
        .zip(recentDubLimited()) { x, y -> Triple(x.first, x.second, y) }
        .asLiveData(viewModelScope.coroutineContext)

    private val isLoading = MutableLiveData<State>()
    private val searchSuggestions = MutableLiveData<List<AnimeSearchResult>>()
    private val searchIsLoading = MutableLiveData<State>()

    init {
        viewModelScope.launch {
            val lastUpdate = frontPageRepository.lastUpdate()
            Log.i(TAG, "last update : $lastUpdate")
            if (lastUpdate == null || lastUpdate.isAfter(LocalDateTime.now())) {
                refreshAllPages()
                Log.i(TAG, "Refreshing front page anime tiles")
            }
        }
    }

    fun refreshAllPages() {
        isLoading.postValue(State.LOADING())
        viewModelScope.launch {
            try {
                frontPageRepository.fetchAll()
                isLoading.postValue(State.SUCCESS())
                Log.i(TAG, "All anime's were successfully fetched from remote")
            } catch (e: Exception) {
                isLoading.postValue(State.FAILED(e))
                Log.e(TAG, "Failed to fetch anime", e)
            }
        }
    }

    fun isLoading(): LiveData<State> = isLoading
    fun searchIsLoading(): LiveData<State> = searchIsLoading

    fun querySearchSuggestions(query: CharSequence) {
        throttleLatest<CharSequence>(coroutineScope = viewModelScope) {
            viewModelScope.launch {
                searchIsLoading.postValue(State.LOADING())
                try {
                    val suggestions = if (TextUtils.isEmpty(query)) {
                        emptyList()
                    } else {
                        searchRepository.search(it.toString()).map {
                            AnimeSearchResult(it)
                        }
                    }
                    searchSuggestions.postValue(suggestions)
                    searchIsLoading.postValue(State.SUCCESS())
                } catch (e: Exception) {
                    searchIsLoading.postValue(State.FAILED(e))
                }
            }
        }(query)
    }

    fun getSearchSuggestions(): LiveData<List<AnimeSearchResult>> = searchSuggestions

    fun getSearchHistory(): List<String> {
        return searchRepository.getSearchHistory()
    }
}