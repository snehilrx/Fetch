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
import com.otaku.kickassanime.db.models.AnimeTile
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class FrontPageListViewModel @Inject constructor(
    repository: FrontPageListRepository
) : ViewModel() {
    val all: LiveData<PagingData<ITileData>> =
        repository.getFrontPageAllPager().liveData.map { it.map { animeTile -> animeTile as ITileData } }.cachedIn(viewModelScope)
    val sub: LiveData<PagingData<ITileData>> =
        repository.getFrontPageSubPager().liveData.map { it.map { animeTile -> animeTile as ITileData } }.cachedIn(viewModelScope)
    val dub: LiveData<PagingData<ITileData>> =
        repository.getFrontPageDubPager().liveData.map { it.map { animeTile -> animeTile as ITileData } }.cachedIn(viewModelScope)

}