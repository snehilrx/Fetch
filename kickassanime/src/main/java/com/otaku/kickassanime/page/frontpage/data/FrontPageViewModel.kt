package com.otaku.kickassanime.page.frontpage.data

import android.util.Log
import androidx.lifecycle.*
import com.otaku.fetch.base.TAG
import com.otaku.fetch.base.livedata.State
import com.otaku.kickassanime.db.models.AnimeTile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.zip
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class FrontPageViewModel @Inject constructor(private val frontPageRepository: FrontPageRepository) :
    ViewModel() {

    private val all = frontPageRepository.getFrontAllPage()
    private val sub = frontPageRepository.getFrontSubPage()
    private val dub = frontPageRepository.getFrontDubPage()

    val zipped = all.zip(sub) { x, y -> Pair(x, y) }.zip(dub) { x, y -> FrontPageData(x.first, x.second, y) }.asLiveData(viewModelScope.coroutineContext, )

    private val isLoading: MutableLiveData<State> = MutableLiveData()

    init {
        viewModelScope.launch {
            val lastUpdate = frontPageRepository.lastUpdate()
            Log.i(TAG, "last update : $lastUpdate")
            if (lastUpdate == null || lastUpdate.isAfter(LocalDateTime.now())) {
                refreshAllPages()
                Log.i(TAG, "Refreshing front page anime tiles")
            }
        }
    }

    fun refreshAllPages() {
        isLoading.postValue(State.LOADING())
        viewModelScope.launch {
            try {
                frontPageRepository.fetchAll()
                frontPageRepository.fetchDub()
                frontPageRepository.fetchSub()
                isLoading.postValue(State.SUCCESS())
                Log.i(TAG, "All anime's were successfully fetched from remote")
            } catch (e: Exception) {
                isLoading.postValue(State.FAILED(e))
                Log.e(TAG, "Failed to fetch anime", e)
            }
        }
    }

    fun isLoading(): LiveData<State> = isLoading

    class FrontPageData(val all: List<AnimeTile>, val sub: List<AnimeTile>, val dub: List<AnimeTile>)
}