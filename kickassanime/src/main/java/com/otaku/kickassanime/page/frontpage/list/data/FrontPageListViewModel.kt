package com.otaku.kickassanime.page.frontpage.list.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.liveData
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
    val all: Flow<PagingData<ITileData>> =
        repository.getFrontPageAllPager().flow.map { it.map { animeTile -> animeTile as ITileData } }.cachedIn(viewModelScope)

    @Suppress("USELESS_CAST")
    val sub: Flow<PagingData<ITileData>> =
        repository.getFrontPageSubPager().flow.map { it.map { animeTile -> animeTile as ITileData } }.cachedIn(viewModelScope)

    @Suppress("USELESS_CAST")
    val dub: Flow<PagingData<ITileData>> =
        repository.getFrontPageDubPager().flow.map { it.map { animeTile -> animeTile as ITileData } }.cachedIn(viewModelScope)

}