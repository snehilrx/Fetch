package com.otaku.kickassanime.page.frontpage.list.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.liveData
import com.otaku.kickassanime.db.models.AnimeTile
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class FrontPageListViewModel @Inject constructor(
    repository: FrontPageListRepository
) : ViewModel() {
    val all: LiveData<PagingData<AnimeTile>> =
        repository.getFrontPageAllPager().liveData.cachedIn(viewModelScope)
    val sub: LiveData<PagingData<AnimeTile>> =
        repository.getFrontPageSubPager().liveData.cachedIn(viewModelScope)
    val dub: LiveData<PagingData<AnimeTile>> =
        repository.getFrontPageDubPager().liveData.cachedIn(viewModelScope)

}