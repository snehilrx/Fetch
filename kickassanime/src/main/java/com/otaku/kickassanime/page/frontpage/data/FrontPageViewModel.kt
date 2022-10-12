package com.otaku.kickassanime.page.frontpage.data

import android.util.Log
import androidx.lifecycle.*
import com.otaku.kickassanime.db.models.AnimeTile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class FrontPageViewModel @Inject constructor(private val frontPageRepository: FrontPageRepository) : ViewModel() {

    var appbarOffset: Int = 0

    val all: LiveData<List<AnimeTile>> =
        frontPageRepository.getFrontAllPage().asLiveData(viewModelScope.coroutineContext)
    val sub: LiveData<List<AnimeTile>> =
        frontPageRepository.getFrontSubPage().asLiveData(viewModelScope.coroutineContext)
    val dub: LiveData<List<AnimeTile>> =
        frontPageRepository.getFrontDubPage().asLiveData(viewModelScope.coroutineContext)

    private val isLoading: MutableLiveData<State> = MutableLiveData()

    init {
        viewModelScope.launch {
            val lastUpdate = frontPageRepository.lastUpdate()
            Log.i(TAG, "last update : $lastUpdate")
            if(lastUpdate == null || lastUpdate.isAfter(LocalDateTime.now())){
                refreshAllPages()
                Log.i(TAG, "Refreshing front page anime tiles")
            }
        }
    }

    fun refreshAllPages(){
        isLoading.postValue(State.LOADING())
        viewModelScope.launch {
            try {
                frontPageRepository.fetchAll()
                frontPageRepository.fetchDub()
                frontPageRepository.fetchSub()
                isLoading.postValue(State.SUCCESS())
                Log.i(TAG, "All anime's were successfully fetched from remote")
            }catch (e: Exception){
                isLoading.postValue(State.FAILED(e))
                Log.e(TAG, "Failed to fetch anime", e)
            }
        }
    }

    fun isLoading() : LiveData<State>  = isLoading

    sealed class State(val exception: Exception?) {
        class LOADING : State(null)
        class SUCCESS : State(null)
        class FAILED(exception: Exception) : State(exception)
    }

    companion object {
        private const val TAG = "FrontPageViewModel"
    }

}