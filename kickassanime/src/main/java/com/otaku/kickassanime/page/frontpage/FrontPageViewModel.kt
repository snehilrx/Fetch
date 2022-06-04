package com.otaku.kickassanime.page.frontpage

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
class FrontPageViewModel @Inject constructor(frontPageRepository: FrontPageRepository) : ViewModel() {

    val all: LiveData<PagingData<AnimeTile>> =
        frontPageRepository.getFrontPageAllPager().liveData.cachedIn(viewModelScope)
    val dubs: LiveData<PagingData<AnimeTile>> =
        frontPageRepository.getFrontPageDubPager().liveData.cachedIn(viewModelScope)
    val subs: LiveData<PagingData<AnimeTile>> =
        frontPageRepository.getFrontPageSubPager().liveData.cachedIn(viewModelScope)

}