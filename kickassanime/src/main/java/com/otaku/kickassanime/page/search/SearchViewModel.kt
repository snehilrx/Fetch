package com.otaku.kickassanime.page.search

import androidx.lifecycle.*
import androidx.paging.*
import com.otaku.fetch.base.livedata.State
import com.otaku.fetch.data.ITileData
import com.otaku.kickassanime.api.model.AnimeSearchResponse
import com.otaku.kickassanime.db.KickassAnimeDb
import com.otaku.kickassanime.utils.asAnimeEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(private val searchRepository: SearchRepository) : ViewModel() {

    inner class SearchPagingSource(
        private val searchRepository: SearchRepository,
        val query: String
    ) : PagingSource<Int, ITileData>() {
        override suspend fun load(
            params: LoadParams<Int>
        ): LoadResult<Int, ITileData> {
            return try {
                val response = searchRepository.search(query)
                LoadResult.Page(
                    data = response,
                    prevKey = null, // Only paging forward.
                    nextKey = null
                )
            } catch (e: Exception) {
                LoadResult.Error(e)
            }
        }

        override fun getRefreshKey(state: PagingState<Int, ITileData>): Int {
            return 0
        }
    }


    fun doSearch(query: String): LiveData<PagingData<ITileData>> {
        searchRepository.addToSearchHistory(query)
        return Pager(
            PagingConfig(pageSize = 200)
        ) {
            SearchPagingSource(searchRepository, query)
        }.liveData.cachedIn(viewModelScope)
    }
}