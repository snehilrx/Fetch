package com.otaku.kickassanime.page.history

import androidx.lifecycle.ViewModel
import androidx.paging.cachedIn
import androidx.paging.liveData
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(historyRepository: HistoryRepository) : ViewModel() {
    val recents = historyRepository.recents.liveData.cachedIn(this)
}