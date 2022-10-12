package com.otaku.kickassanime.page.favourtites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.otaku.kickassanime.page.search.SearchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavouritesViewModel @Inject constructor(private val repository: FavouritesRepository): ViewModel() {
    fun removeFavourite(animeSlugId: Int) {
        viewModelScope.launch {
            repository.removeFavourite(animeSlugId)
        }
    }

    val favourites = repository.pager.asLiveData(viewModelScope.coroutineContext).cachedIn(viewModelScope)

}
