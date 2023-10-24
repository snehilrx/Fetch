package com.otaku.kickassanime.page.favourtites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavouritesViewModel @Inject constructor(private val repository: FavouritesRepository) :
    ViewModel() {
    fun removeFavourite(animeSlug: String) {
        viewModelScope.launch {
            repository.removeFavourite(animeSlug)
        }
    }

    val favourites = repository.pager.cachedIn(viewModelScope)

}
