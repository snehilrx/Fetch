package com.otaku.kickassanime.page.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(historyRepository: HistoryRepository) : ViewModel() {
    val recent = historyRepository.recents.flow.cachedIn(this.viewModelScope)
}