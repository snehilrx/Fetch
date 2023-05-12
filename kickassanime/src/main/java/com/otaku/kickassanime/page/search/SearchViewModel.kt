package com.otaku.kickassanime.page.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.otaku.fetch.data.ITileData
import com.otaku.kickassanime.db.models.AnimeSearchResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(private val searchRepository: SearchRepository) :
    ViewModel() {


    fun doSearch(
        query: String,
        genre: List<String>? = null,
        language: List<String>? = null,
        year: Int? = null,
        status: String? = null,
        type: String? = null
    ): Flow<PagingData<out ITileData>> {
        searchRepository.addToSearchHistory(query)
        return searchRepository.getSearchPager(
            query, genre, language, year, status, type
        ).flow.map flowMap@{ anime ->
            anime.map {
                AnimeSearchResult(it.animeEntity)
            }
        }.cachedIn(viewModelScope)
    }

    fun getFilters() {
        viewModelScope.launch {
        }
    }
}