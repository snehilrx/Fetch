package com.otaku.kickassanime.page.frontpage.data

import android.text.TextUtils
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.viewModelScope
import com.lapism.search.widget.MaterialSearchView
import com.otaku.fetch.base.TAG
import com.otaku.fetch.base.livedata.DebouncedLiveData
import com.otaku.fetch.base.livedata.GenericState
import com.otaku.fetch.base.livedata.State
import com.otaku.kickassanime.db.models.AnimeSearchResult
import com.otaku.kickassanime.page.search.SearchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
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
    private val searchSuggestions = MutableLiveData<GenericState<List<AnimeSearchResult>>>()

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
    fun getSearchSuggestions(): LiveData<GenericState<List<AnimeSearchResult>>> =
        DebouncedLiveData(
            searchSuggestions.distinctUntilChanged(),
            300,
            viewModelScope.coroutineContext
        )

    fun getSearchHistory(): List<String> {
        return searchRepository.getSearchHistory()
    }

    @OptIn(FlowPreview::class)
    fun transformToQueryFlow(
        scope: CoroutineScope,
        registerCallback: (MaterialSearchView.OnQueryTextListener) -> Unit,
        unregisterCallback: () -> Unit,
    ) {
        scope.launch {
            callbackFlow {
                val onQueryTextChange = object : MaterialSearchView.OnQueryTextListener {
                    override fun onQueryTextChange(newText: CharSequence) {
                        trySend(newText.toString())
                    }

                    override fun onQueryTextSubmit(query: CharSequence) {
                        // no-op as we need only text change listener
                    }
                }
                registerCallback(onQueryTextChange)
                awaitClose { unregisterCallback() }
            }
                .conflate()
                .debounce(300).distinctUntilChanged { x, y ->
                    x.equals(y, true)
                }
                .collectLatest { query ->
                    viewModelScope.launch {
                        searchSuggestions.postValue(GenericState.LOADING())
                        try {
                            val suggestions = if (TextUtils.isEmpty(query)) {
                                emptyList()
                            } else {
                                searchRepository.search(query).map {
                                    AnimeSearchResult(it)
                                }
                            }
                            searchSuggestions.postValue(GenericState.SUCCESS(suggestions))
                        } catch (e: Exception) {
                            searchSuggestions.postValue(GenericState.FAILED(e))
                        }
                    }
                }
        }
    }

    fun addToSearchHistory(query: String) {
        viewModelScope.launch {
            searchRepository.addToSearchHistory(query)
        }
    }
}