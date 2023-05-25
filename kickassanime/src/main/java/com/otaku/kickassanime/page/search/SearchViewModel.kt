package com.otaku.kickassanime.page.search

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.otaku.fetch.base.livedata.GenericState
import com.otaku.fetch.data.ITileData
import com.otaku.kickassanime.api.model.Filters
import com.otaku.kickassanime.db.models.AnimeSearchResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchRepository: SearchRepository, private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    var genresState: Array<MutableState<Boolean>>? = null
    var typesState: Array<MutableState<Boolean>>? = null
    var yearsState: Array<MutableState<Boolean>>? = null

    val searchPager: Flow<PagingData<out ITileData>> =
        searchRepository.pager.flow.map flowMap@{ anime ->
            anime.map {
                AnimeSearchResult(it.animeEntity)
            }
        }.cachedIn(viewModelScope)

    private val filtersLiveData = MutableLiveData<GenericState<Filters>>()

    fun doSearch(
        query: String, genre: List<String>? = null, year: Int? = null, type: String? = null
    ) {
        searchRepository.search(query, genre, year, type)
    }

    fun loadFilters() {
        filtersLiveData.postValue(GenericState.LOADING())
        viewModelScope.launch {
            try {
                val filters =
                    savedStateHandle.get<Filters>(FILTERS) ?: searchRepository.getFilters()
                if (!savedStateHandle.contains(FILTERS)) savedStateHandle[FILTERS] = filters
                genresState = filters.genres?.map { mutableStateOf(false) }?.toTypedArray()
                typesState = filters.types?.map { mutableStateOf(false) }?.toTypedArray()
                yearsState = filters.years?.map { mutableStateOf(false) }?.toTypedArray()
                filtersLiveData.postValue(GenericState.SUCCESS(filters))
            } catch (e: Exception) {
                filtersLiveData.postValue(GenericState.FAILED(e))
            }
        }
    }

    fun getFilters(): LiveData<GenericState<Filters>> = filtersLiveData

    companion object {
        const val FILTERS = "filters"
    }
}