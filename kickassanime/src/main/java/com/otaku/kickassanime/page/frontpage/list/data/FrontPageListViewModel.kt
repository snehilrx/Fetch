package com.otaku.kickassanime.page.frontpage.list.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.otaku.fetch.data.ITileData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class FrontPageListViewModel @Inject constructor(
    repository: FrontPageListRepository
) : ViewModel() {

    @Suppress("USELESS_CAST")
    val recent: Flow<PagingData<ITileData>> =
        repository.getRecentPager().flow.map { it.map { animeTile -> animeTile as ITileData } }
            .cachedIn(viewModelScope)

    @Suppress("USELESS_CAST")
    val trending: Flow<PagingData<ITileData>> =
        repository.getSubPager().flow.map { it.map { animeTile -> animeTile as ITileData } }
            .cachedIn(viewModelScope)

    @Suppress("USELESS_CAST")
    val popular: Flow<PagingData<ITileData>> =
        repository.getDubPager().flow.map { it.map { animeTile -> animeTile as ITileData } }
            .cachedIn(viewModelScope)

}